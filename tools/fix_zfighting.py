"""Find and fix z-fighting (flickering) in Bedrock .geo.json models.

Z-fighting happens where two faces facing the same way lie on the same plane and overlap.
Only cubes that move together are compared (animated bones such as wheels or turrets are
checked within themselves). The fix nudges the smaller face outward by 0.04 px per level,
so the model looks the same but the renderer always draws one face on top.

Usage:
  python3 tools/fix_zfighting.py scan <model.geo.json>...   # report overlapping face pairs
  python3 tools/fix_zfighting.py fix  <model.geo.json>...   # fix in place, keeps indentation
"""
import json, sys
from collections import defaultdict
EPS_PLANE, MIN_AREA = 1e-4, 1e-3
ANIM = ('wheel', 'Wheel', 'move_', 'turret', 'barrel', 'flare', 'weapon', 'aft', 'jiqiang', 'zhu', 'steering', 'rws', 'hatch')
def load(p):
    raw = open(p).read(); return raw, json.loads(raw)
def group_key(b, c, parent_of):
    # walk up to find the nearest animated or rotated bone; cubes under it move with it
    n = b['name']; chain = []
    while n:
        bb = parent_of[n]; chain.append(n)
        if n.startswith(ANIM) or (bb.get('rotation') and any(bb['rotation'])): break
        n = bb.get('parent')
    owner = chain[-1] if (n and (n.startswith(ANIM) or (parent_of[n].get('rotation') and any(parent_of[n]['rotation'])))) else 'static'
    rot = tuple(round(x, 4) for x in (c.get('rotation') or [0, 0, 0]))
    piv = tuple(round(x, 4) for x in c.get('pivot', [0, 0, 0])) if any(rot) else ()
    return (owner, rot, piv)
def faces(c):
    o, s, i = c['origin'], c['size'], c.get('inflate', 0)
    lo = [o[k] - i for k in range(3)]; hi = [o[k] + s[k] + i for k in range(3)]
    for ax in range(3):
        u, v = [k for k in range(3) if k != ax]
        rect = (lo[u], hi[u], lo[v], hi[v])
        yield (ax, -1, lo[ax], rect); yield (ax, 1, hi[ax], rect)
def scan(g):
    bones = {b['name']: b for b in g['bones']}
    groups = defaultdict(list)
    for b in g['bones']:
        for idx, c in enumerate(b.get('cubes', [])):
            groups[group_key(b, c, bones)].append((b['name'], idx, c))
    hits = []
    for key, cs in groups.items():
        planes = defaultdict(list)
        for bn, idx, c in cs:
            for ax, sg, pos, rect in faces(c):
                planes[(ax, sg, round(pos / EPS_PLANE))].append((bn, idx, c, rect))
        for (ax, sg, _), fs in planes.items():
            for a in range(len(fs)):
                for b2 in range(a + 1, len(fs)):
                    r1, r2 = fs[a][3], fs[b2][3]
                    w = min(r1[1], r2[1]) - max(r1[0], r2[0]); h = min(r1[3], r2[3]) - max(r1[2], r2[2])
                    if w > 0 and h > 0 and w * h > MIN_AREA:
                        hits.append((ax, sg, fs[a], fs[b2], w * h))
    return hits
if __name__ == '__main__':
    mode, paths = sys.argv[1], sys.argv[2:]
    for p in paths:
        raw, geo = load(p); g = geo['minecraft:geometry'][0]
        hits = scan(g)
        pairs = defaultdict(int)
        for ax, sg, f1, f2, area in hits: pairs[(f1[0], f2[0])] += 1
        print(f'{p.split("/")[-1]}: {len(hits)} overlapping coplanar face pairs', dict(sorted(pairs.items(), key=lambda x: -x[1])[:8]))

def fix(g, eps=0.04, skip=('waterMask',)):
    bones = {b['name']: b for b in g['bones']}
    groups = defaultdict(list)
    for b in g['bones']:
        if b['name'] in skip: continue
        for c in b.get('cubes', []):
            groups[group_key(b, c, bones)].append(c)
    pushes = []  # (cube, axis, sign, amount)
    for cs in groups.values():
        planes = defaultdict(list)
        for c in cs:
            for ax, sg, pos, rect in faces(c):
                planes[(ax, sg, round(pos / EPS_PLANE))].append((c, rect))
        for (ax, sg, _), fs in planes.items():
            if len(fs) < 2: continue
            # greedy colouring of the overlap graph: biggest face keeps its place, each overlapping face
            # gets the smallest push level not used by a bigger face it overlaps
            def ov(r, r2):
                w = min(r[1], r2[1]) - max(r[0], r2[0]); h = min(r[3], r2[3]) - max(r[2], r2[2])
                return w > 0 and h > 0 and w * h > MIN_AREA
            order = sorted(range(len(fs)), key=lambda i: -(fs[i][1][1] - fs[i][1][0]) * (fs[i][1][3] - fs[i][1][2]))
            level = {}
            for i in order:
                used = {level[j] for j in level if ov(fs[i][1], fs[j][1])}
                lv = 0
                while lv in used: lv += 1
                level[i] = lv
                if lv: pushes.append((fs[i][0], ax, sg, lv * eps))
    for c, ax, sg, d in pushes:
        o, s = c['origin'], c['size']
        if sg < 0: o[ax] = round(o[ax] - d, 6)
        s[ax] = round(s[ax] + d, 6)
    return len(pushes)
if __name__ == '__main__' and sys.argv[1] == 'fix':
    for p in sys.argv[2:]:
        raw, geo = load(p); indent = '\t' if '\n\t' in raw else 1
        for _ in range(4):
            n = fix(geo['minecraft:geometry'][0])
            left = len([h for h in scan(geo['minecraft:geometry'][0]) if 'waterMask' not in (h[2][0], h[3][0])])
            if left == 0: break
        json.dump(geo, open(p, 'w'), indent=indent)
        print(p, 'remaining after fix:', left)
