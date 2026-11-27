"""Find and fix z-fighting (flickering) in Bedrock .geo.json models.

Z-fighting happens where two faces facing the same way lie on (almost) the same plane and
overlap, e.g. the side faces of the rotated segments that make up a round tyre. Faces are
compared in model space after applying each cube's rotation, so cubes with different
rotations are handled too. Only cubes that move together are compared (an animated bone
such as a wheel or turret is checked within itself). Faces closer than 0.02 px along their
normal count as coplanar.

The fix gives every group of overlapping faces a level: the biggest face keeps level 0 and
each overlapping face takes the lowest level none of its overlapping neighbours uses (a
ring of tyre segments alternates 0/1). A face at level L is pushed outward to sit
L x 0.04 px in front of the group, so the model looks the same but one face always wins.
waterMask is skipped: it only writes depth to hide water.

Usage:
  python3 tools/fix_zfighting.py scan <model.geo.json>...   # report overlapping face pairs
  python3 tools/fix_zfighting.py fix  <model.geo.json>...   # fix in place, keeps indentation
"""
import json
import math
import sys
from collections import defaultdict

TOL = 0.02        # px: faces closer than this along their normal count as coplanar
EPS = 0.04        # px: step between levels of nudged faces
MIN_AREA = 1e-3   # px^2: ignore overlaps smaller than this
SKIP = ('waterMask',)
ANIM = ('wheel', 'Wheel', 'move_', 'turret', 'barrel', 'flare', 'weapon', 'aft', 'jiqiang', 'zhu',
        'steering', 'rws', 'hatch')
IDENTITY = [[1, 0, 0], [0, 1, 0], [0, 0, 1]]


def _rot_matrix(r):
    """Bedrock cube rotation [x, y, z] degrees as a 3x3 matrix (R = Rz(-z) * Ry(y) * Rx(-x))."""
    ax, ay, az = (math.radians(v) for v in r)
    cx, sx = math.cos(-ax), math.sin(-ax)
    cy, sy = math.cos(ay), math.sin(ay)
    cz, sz = math.cos(-az), math.sin(-az)
    rx = [[1, 0, 0], [0, cx, -sx], [0, sx, cx]]
    ry = [[cy, 0, sy], [0, 1, 0], [-sy, 0, cy]]
    rz = [[cz, -sz, 0], [sz, cz, 0], [0, 0, 1]]
    mul = lambda a, b: [[sum(a[i][k] * b[k][j] for k in range(3)) for j in range(3)] for i in range(3)]
    return mul(rz, mul(ry, rx))


def _owner(bone, bones):
    """Name of the nearest animated ancestor bone (it moves at runtime), or 'static'."""
    n = bone['name']
    while n:
        if n.startswith(ANIM):
            return n
        n = bones[n].get('parent')
    return 'static'


def _bone_transform(name, bones, cache):
    """Bind-pose (matrix, offset) of a bone: its own rotation about its pivot, after its parents'."""
    if name in cache:
        return cache[name]
    b = bones[name]
    rot = b.get('rotation') or [0, 0, 0]
    m = _rot_matrix(rot) if any(rot) else IDENTITY
    p = b.get('pivot', [0, 0, 0])
    t = [p[i] - sum(m[i][k] * p[k] for k in range(3)) for i in range(3)]   # x -> m x + t
    if b.get('parent'):
        pm, pt = _bone_transform(b['parent'], bones, cache)
        m2 = [[sum(pm[i][k] * m[k][j] for k in range(3)) for j in range(3)] for i in range(3)]
        t = [sum(pm[i][k] * t[k] for k in range(3)) + pt[i] for i in range(3)]
        m = m2
    cache[name] = (m, t)
    return m, t


def _faces(g):
    """Every cube face as a dict with its owner group, normal, plane offset and corner points."""
    bones = {b['name']: b for b in g['bones']}
    cache = {}
    out = []
    for b in g['bones']:
        if b['name'] in SKIP:
            continue
        owner = _owner(b, bones)
        bm, bt = _bone_transform(b['name'], bones, cache)
        for c in b.get('cubes', []):
            rot = c.get('rotation') or [0, 0, 0]
            m = _rot_matrix(rot) if any(rot) else IDENTITY
            p = c.get('pivot', [0, 0, 0]) if any(rot) else [0, 0, 0]
            o, s, inf = c['origin'], c['size'], c.get('inflate', 0)
            lo = [o[k] - inf for k in range(3)]
            hi = [o[k] + s[k] + inf for k in range(3)]
            def to_model(q, m=m, p=p, bm=bm, bt=bt):
                q = [sum(m[i][k] * (q[k] - p[k]) for k in range(3)) + p[i] for i in range(3)]
                return [sum(bm[i][k] * q[k] for k in range(3)) + bt[i] for i in range(3)]
            for ax in range(3):
                u, v = [k for k in range(3) if k != ax]
                for sg, val in ((-1, lo[ax]), (1, hi[ax])):
                    quad = []
                    for a, bb in ((lo[u], lo[v]), (hi[u], lo[v]), (hi[u], hi[v]), (lo[u], hi[v])):
                        q = [0.0, 0.0, 0.0]
                        q[ax], q[u], q[v] = val, a, bb
                        quad.append(to_model(q))
                    n = [sum(bm[i][k] * m[k][ax] for k in range(3)) * sg for i in range(3)]
                    out.append({'owner': owner, 'bone': b['name'], 'cube': c, 'ax': ax, 'sg': sg, 'n': n,
                                'd': sum(n[i] * quad[0][i] for i in range(3)), 'quad': quad,
                                'area': (hi[u] - lo[u]) * (hi[v] - lo[v])})
    return out


def _project(quad, n):
    a = [1, 0, 0] if abs(n[0]) < 0.9 else [0, 1, 0]
    e1 = [n[1] * a[2] - n[2] * a[1], n[2] * a[0] - n[0] * a[2], n[0] * a[1] - n[1] * a[0]]
    l = math.sqrt(sum(x * x for x in e1))
    e1 = [x / l for x in e1]
    e2 = [n[1] * e1[2] - n[2] * e1[1], n[2] * e1[0] - n[0] * e1[2], n[0] * e1[1] - n[1] * e1[0]]
    return [(sum(q[i] * e1[i] for i in range(3)), sum(q[i] * e2[i] for i in range(3))) for q in quad]


def _area(poly):
    return abs(sum(poly[i][0] * poly[i - 1][1] - poly[i - 1][0] * poly[i][1] for i in range(len(poly)))) / 2


def _clip(subject, clipper):
    """Sutherland-Hodgman intersection of two convex polygons."""
    orient = 1 if sum(clipper[i][0] * clipper[i - 1][1] - clipper[i - 1][0] * clipper[i][1]
                      for i in range(len(clipper))) < 0 else -1
    inside = lambda p, a, b: orient * ((b[0] - a[0]) * (p[1] - a[1]) - (b[1] - a[1]) * (p[0] - a[0])) >= -1e-9

    def cross(p, q, a, b):
        den = (p[0] - q[0]) * (a[1] - b[1]) - (p[1] - q[1]) * (a[0] - b[0])
        if abs(den) < 1e-12:
            return q
        t = ((p[0] - a[0]) * (a[1] - b[1]) - (p[1] - a[1]) * (a[0] - b[0])) / den
        return (p[0] + t * (q[0] - p[0]), p[1] + t * (q[1] - p[1]))

    out = subject
    for i in range(len(clipper)):
        a, b = clipper[i - 1], clipper[i]
        inp, out = out, []
        for j in range(len(inp)):
            cur, prev = inp[j], inp[j - 1]
            if inside(cur, a, b):
                if not inside(prev, a, b):
                    out.append(cross(prev, cur, a, b))
                out.append(cur)
            elif inside(prev, a, b):
                out.append(cross(prev, cur, a, b))
        if not out:
            break
    return out


def _overlap(f1, f2):
    poly = _clip(_project(f1['quad'], f1['n']), _project(f2['quad'], f1['n']))
    return _area(poly) if len(poly) >= 3 else 0.0


def _pairs(g, reach):
    """Overlapping same-facing face pairs whose planes are closer than `reach`."""
    buckets = defaultdict(list)
    for f in _faces(g):
        buckets[(f['owner'], tuple(round(x, 3) for x in f['n']))].append(f)
    pairs = []
    for fs in buckets.values():
        fs.sort(key=lambda f: f['d'])
        for i in range(len(fs)):
            for j in range(i + 1, len(fs)):
                if fs[j]['d'] - fs[i]['d'] >= reach:
                    break
                if fs[i]['cube'] is not fs[j]['cube'] and _overlap(fs[i], fs[j]) > MIN_AREA:
                    pairs.append((fs[i], fs[j]))
    return pairs


def scan(g):
    return _pairs(g, TOL)


def fix(g):
    """Assign levels to overlapping coplanar faces and push them apart; returns faces moved."""
    pairs = _pairs(g, TOL)
    nbr = defaultdict(list)
    for a, b in pairs:
        nbr[id(a)].append(b)
        nbr[id(b)].append(a)
    faces = {id(f): f for p in pairs for f in p}
    seen, moved = set(), 0
    for fid in faces:
        if fid in seen:
            continue
        comp, stack = [], [faces[fid]]
        seen.add(fid)
        while stack:
            f = stack.pop()
            comp.append(f)
            for q in nbr[id(f)]:
                if id(q) not in seen:
                    seen.add(id(q))
                    stack.append(q)
        top = max(f['d'] for f in comp)
        level = {}
        for f in sorted(comp, key=lambda f: -f['area']):
            used = {level[id(q)] for q in nbr[id(f)] if id(q) in level}
            lv = 0
            while lv in used:
                lv += 1
            level[id(f)] = lv
            if lv:
                delta = top + lv * EPS - f['d']
                c, ax, sg = f['cube'], f['ax'], f['sg']
                if sg < 0:
                    c['origin'][ax] = round(c['origin'][ax] - delta, 6)
                c['size'][ax] = round(c['size'][ax] + delta, 6)
                moved += 1
    return moved


def _load(path):
    raw = open(path).read()
    return json.loads(raw), ('\t' if '\n\t' in raw else 1)


if __name__ == '__main__':
    mode, paths = sys.argv[1], sys.argv[2:]
    for p in paths:
        geo, indent = _load(p)
        g = geo['minecraft:geometry'][0]
        if mode == 'fix':
            for _ in range(6):
                if not fix(g):
                    break
            json.dump(geo, open(p, 'w'), indent=indent)
        counts = defaultdict(int)
        for a, b in scan(g):
            counts[tuple(sorted((a['bone'], b['bone'])))] += 1
        print(f'{p.split("/")[-1]}: {sum(counts.values())} overlapping coplanar face pairs', dict(counts) if counts else '')
