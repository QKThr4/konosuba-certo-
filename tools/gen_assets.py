#!/usr/bin/env python3
"""Generates all textures, models, lang and data JSON for KonoRPG. Run from the project root."""
import json, os, random, struct, zlib

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', 'src', 'main', 'resources')
A = os.path.join(ROOT, 'assets', 'konorpg')
D = os.path.join(ROOT, 'data', 'konorpg')


def w(path, content):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    mode = 'wb' if isinstance(content, bytes) else 'w'
    with open(path, mode, **({} if mode == 'wb' else {'encoding': 'utf-8'})) as f:
        f.write(content)


def js(path, obj):
    w(path, json.dumps(obj, ensure_ascii=False, indent=1))


def png(path, W, H, px):
    raw = b''
    for y in range(H):
        raw += b'\x00' + b''.join(bytes(px[y][x]) for x in range(W))
    def chunk(t, d):
        c = struct.pack('>I', len(d)) + t + d
        return c + struct.pack('>I', zlib.crc32(t + d) & 0xffffffff)
    data = b'\x89PNG\r\n\x1a\n' + chunk(b'IHDR', struct.pack('>IIBBBBB', W, H, 8, 6, 0, 0, 0)) + chunk(b'IDAT', zlib.compress(raw, 9)) + chunk(b'IEND', b'')
    w(path, data)


def canvas(W, H, c=(0, 0, 0, 0)):
    return [[c for _ in range(W)] for _ in range(H)]


def rect(img, x, y, ww, hh, c):
    for j in range(y, y + hh):
        for i in range(x, x + ww):
            if 0 <= j < len(img) and 0 <= i < len(img[0]):
                img[j][i] = c


def rgb(h):
    h = h.lstrip('#')
    return (int(h[0:2], 16), int(h[2:4], 16), int(h[4:6], 16), 255)


def shade(c, f):
    if isinstance(c, str):
        c = rgb(c)
    return (max(0, min(255, int(c[0] * f))), max(0, min(255, int(c[1] * f))), max(0, min(255, int(c[2] * f))), 255)

# ---------------------------------------------------------------- skins
FACES = {
    'head': [(8, 0, 8, 8), (16, 0, 8, 8), (0, 8, 8, 8), (8, 8, 8, 8), (16, 8, 8, 8), (24, 8, 8, 8)],
    'body': [(20, 16, 8, 4), (28, 16, 8, 4), (16, 20, 4, 12), (20, 20, 8, 12), (28, 20, 4, 12), (32, 20, 8, 12)],
    'rleg': [(4, 16, 4, 4), (8, 16, 4, 4), (0, 20, 4, 12), (4, 20, 4, 12), (8, 20, 4, 12), (12, 20, 4, 12)],
    'rarm': [(44, 16, 4, 4), (48, 16, 4, 4), (40, 20, 4, 12), (44, 20, 4, 12), (48, 20, 4, 12), (52, 20, 4, 12)],
    'lleg': [(20, 48, 4, 4), (24, 48, 4, 4), (16, 52, 4, 12), (20, 52, 4, 12), (24, 52, 4, 12), (28, 52, 4, 12)],
    'larm': [(36, 48, 4, 4), (40, 48, 4, 4), (32, 52, 4, 12), (36, 52, 4, 12), (40, 52, 4, 12), (44, 52, 4, 12)],
}


def make_skin(name, skin, hair, top, bottom, boots, eye='#222222', long_hair=False, extra=None, scales=False, bones=False):
    img = canvas(64, 64)
    skin, hair, top, bottom, boots = map(rgb, (skin, hair, top, bottom, boots))
    for part in ('head', 'body', 'rleg', 'rarm', 'lleg', 'larm'):
        base = {'head': skin, 'body': top, 'rleg': bottom, 'lleg': bottom, 'rarm': top, 'larm': top}[part]
        for k, (u, v, ww, hh) in enumerate(FACES[part]):
            rect(img, u, v, ww, hh, shade(base, 1.0 if k != 1 else 0.7))
    # hair
    for k, (u, v, ww, hh) in enumerate(FACES['head']):
        if k in (0, 4, 2, 5):
            rect(img, u, v, ww, hh, hair)
    u, v, _, _ = FACES['head'][3]
    rect(img, u, v, 8, 2, hair)  # bangs
    rect(img, u, v + 2, 1, 2, hair); rect(img, u + 7, v + 2, 1, 2, hair)
    # face
    rect(img, u + 1, v + 4, 2, 1, (255, 255, 255, 255)); rect(img, u + 5, v + 4, 2, 1, (255, 255, 255, 255))
    ec = rgb(eye)
    img[v + 4][u + 2] = ec; img[v + 4][u + 5] = ec
    rect(img, u + 3, v + 6, 2, 1, shade(skin, 0.6))
    if long_hair:
        bu, bv, bw, bh = FACES['body'][5]
        rect(img, bu, bv, bw, 8, hair)
        for (u2, v2, w2, h2) in (FACES['rarm'][5], FACES['larm'][5]):
            rect(img, u2, v2, w2, 3, hair)
    # hands (skin) + boots
    for part in ('rarm', 'larm'):
        for (u2, v2, w2, h2) in FACES[part][2:]:
            rect(img, u2, v2 + 9, w2, 3, skin)
    for part in ('rleg', 'lleg'):
        for (u2, v2, w2, h2) in FACES[part][2:]:
            rect(img, u2, v2 + 9, w2, 3, rgb(boots) if isinstance(boots, str) else boots)
    # belt
    for (u2, v2, w2, h2) in FACES['body'][2:]:
        rect(img, u2, v2 + 9, w2, 1, shade(bottom, 0.6))
    if extra == 'apron':
        u2, v2, _, _ = FACES['body'][3]
        rect(img, u2 + 1, v2 + 3, 6, 9, rgb('#e8e0cc'))
    if extra == 'armor':
        for (u2, v2, w2, h2) in FACES['body'][2:]:
            rect(img, u2, v2, w2, 5, rgb('#9aa3ad'))
        rect(img, FACES['head'][0][0], FACES['head'][0][1], 8, 8, rgb('#7d8791'))
        for k in (2, 3, 4, 5):
            u2, v2, w2, h2 = FACES['head'][k]
            rect(img, u2, v2, w2, 3, rgb('#7d8791'))
    if extra == 'robe':
        for part in ('rleg', 'lleg'):
            for (u2, v2, w2, h2) in FACES[part]:
                rect(img, u2, v2, w2, h2, top)
    if scales:
        for part in ('head', 'body', 'rarm', 'larm', 'rleg', 'lleg'):
            for (u2, v2, w2, h2) in FACES[part]:
                for j in range(v2, v2 + h2, 2):
                    for i in range(u2 + (j // 2) % 2, u2 + w2, 2):
                        img[j][i] = shade(img[j][i], 0.82)
    if bones:
        for (u2, v2, w2, h2) in FACES['body'][2:]:
            for j in range(v2 + 1, v2 + 9, 2):
                rect(img, u2, j, w2, 1, rgb('#6e6a58'))
        for part in ('rarm', 'larm', 'rleg', 'lleg'):
            for (u2, v2, w2, h2) in FACES[part][2:]:
                rect(img, u2 + 1, v2, 1, h2, rgb('#6e6a58'))
        rect(img, u + 1, v + 4, 2, 2, (20, 20, 20, 255)); rect(img, u + 5, v + 4, 2, 2, (20, 20, 20, 255))
    png(os.path.join(A, 'textures', 'entity', name + '.png'), 64, 64, img)


SKINS = {
    'luna': dict(skin='#f1c9a5', hair='#7a3fa0', top='#f2f2f2', bottom='#4a2b6b', boots='#3a2a20', eye='#5522aa', long_hair=True),
    'shop_weapon': dict(skin='#e0b088', hair='#2b1a10', top='#a02020', bottom='#4a3a2a', boots='#2a1a10', extra='apron'),
    'shop_armor': dict(skin='#d9a579', hair='#555555', top='#5d6d7d', bottom='#3a3a44', boots='#222222', extra='armor'),
    'shop_magic': dict(skin='#f0d0b0', hair='#e8e8f0', top='#5b2d8e', bottom='#5b2d8e', boots='#2a1a40', eye='#7733cc', extra='robe', long_hair=True),
    'shop_item': dict(skin='#e8c09a', hair='#8a5a2a', top='#3f8f3f', bottom='#5a4a2a', boots='#2a1a10', extra='apron'),
    'blacksmith': dict(skin='#c08860', hair='#222222', top='#5a3a22', bottom='#3a2a1a', boots='#1a1a1a', extra='apron'),
    'alchemist': dict(skin='#f0d8c0', hair='#2a6a6a', top='#f0f0f0', bottom='#2a4a4a', boots='#3a2a20', eye='#116666'),
    'innkeeper': dict(skin='#f0c8a0', hair='#c05a20', top='#d98a2a', bottom='#6a3a1a', boots='#3a2a10', extra='apron', long_hair=True),
    'tavernkeeper': dict(skin='#d8a880', hair='#4a2a10', top='#8a2a2a', bottom='#3a2a2a', boots='#222222', extra='apron'),
    'guard': dict(skin='#e0b088', hair='#333333', top='#7a8794', bottom='#3a4048', boots='#222222', extra='armor'),
    'aqua': dict(skin='#f6d8c0', hair='#4fc3f7', top='#8fd6ff', bottom='#e8f6ff', boots='#4f8fcf', eye='#2a66cc', long_hair=True),
    'goblin': dict(skin='#5a9a3a', hair='#2b4a1a', top='#6a4a2a', bottom='#4a3820', boots='#2a1a10', eye='#ffe040'),
    'kobold': dict(skin='#b5683a', hair='#6a3a1a', top='#7a5a3a', bottom='#4a3a2a', boots='#2a1a10', eye='#ffcc00'),
    'orc': dict(skin='#3f7a2f', hair='#111111', top='#2a2a2a', bottom='#4a3a2a', boots='#111111', eye='#ff3030', extra='armor'),
    'lizardman': dict(skin='#2a9d8f', hair='#1d6f65', top='#264653', bottom='#264653', boots='#1a2a30', eye='#ffee00', scales=True),
    'skeleton_warrior': dict(skin='#e8e4d0', hair='#d8d4c0', top='#d8d4c0', bottom='#d8d4c0', boots='#c8c4b0', eye='#222222', bones=True),
    'giant_toad': dict(skin='#6b8e23', hair='#4f6b18', top='#7a9a30', bottom='#5f7f20', boots='#4f6b18', eye='#ffd700', scales=True),
}
VILL = [('#f1c9a5', '#6a3a1a', '#3a6ea5', '#4a3a2a'), ('#e0b088', '#222222', '#a55a3a', '#3a3a4a'), ('#f0d0b0', '#d9a040', '#7a3a7a', '#4a2a2a'),
        ('#c9966b', '#2a1a10', '#3a8a5a', '#3a3a2a'), ('#e8c09a', '#8a2a2a', '#c9a030', '#4a3a3a'), ('#f6d8c0', '#f0e060', '#e89aa8', '#6a4a4a'),
        ('#d8a880', '#111111', '#5a5a8a', '#2a2a3a'), ('#f1c9a5', '#aaaaaa', '#6a8a3a', '#4a4a2a')]
for i, (s, h, t, b) in enumerate(VILL):
    SKINS['villager_' + 'abcdefgh'[i]] = dict(skin=s, hair=h, top=t, bottom=b, boots='#2a1a10', long_hair=(i % 3 == 2))
for n, kw in SKINS.items():
    make_skin(n, **kw)

# ---------------------------------------------------------------- items
def icon(name, fn):
    img = canvas(16, 16)
    fn(img)
    png(os.path.join(A, 'textures', 'item', name + '.png'), 16, 16, img)


def px(img, x, y, c):
    if 0 <= x < 16 and 0 <= y < 16:
        img[y][x] = c if len(c) == 4 else rgb(c) if isinstance(c, str) else c


def sword(blade, hilt, gem=None, wide=False):
    def f(img):
        for i in range(3, 13):
            px(img, i, 15 - i, blade); px(img, i + 1, 15 - i, shade(blade, 0.8))
            if wide: px(img, i, 14 - i, shade(blade, 1.1) if False else blade)
        for i in range(2, 5):
            px(img, i, 15 - i - 0, hilt)
        px(img, 3, 11, hilt); px(img, 4, 12, hilt); px(img, 2, 12, hilt); px(img, 4, 10, hilt)
        px(img, 1, 14, hilt); px(img, 2, 13, hilt)
        if gem: px(img, 3, 12, gem)
    return f


def axe(head, wood):
    def f(img):
        for i in range(3, 13): px(img, i, 15 - i, wood)
        for (x, y) in [(9, 2), (10, 2), (11, 3), (10, 3), (9, 3), (8, 4), (11, 4), (10, 4), (9, 4), (12, 4)]: px(img, x, y, head)
    return f


def staff(wood, gem):
    def f(img):
        for i in range(3, 14): px(img, i, 15 - i, wood)
        for (x, y) in [(12, 3), (13, 2), (12, 2), (13, 3), (11, 2), (12, 1), (13, 1), (14, 2), (14, 3), (13, 4)]: px(img, x, y, gem)
        px(img, 12, 3, (255, 255, 255, 255))
    return f


def bow(wood):
    def f(img):
        for i in range(2, 14):
            x = 3 + int(3 * (1 - abs(i - 8) / 6.0))
            px(img, x, i, wood)
        for i in range(2, 14): px(img, 12, i, '#dddddd')
    return f


def ring(gem, band='#d4af37'):
    def f(img):
        for (x, y) in [(6, 8), (5, 9), (5, 10), (5, 11), (6, 12), (7, 13), (8, 13), (9, 12), (10, 11), (10, 10), (10, 9), (9, 8), (7, 8), (8, 8)]: px(img, x, y, band)
        for (x, y) in [(7, 6), (8, 6), (7, 7), (8, 7), (6, 7), (9, 7)]: px(img, x, y, gem)
    return f


def necklace(gem, chain='#d4af37'):
    def f(img):
        for i in range(2, 14):
            px(img, i, 2 + int(((i - 8) ** 2) / 5.0), chain)
        for (x, y) in [(7, 9), (8, 9), (7, 10), (8, 10), (7, 11), (8, 11), (6, 10), (9, 10)]: px(img, x, y, gem)
    return f


def potion(color):
    def f(img):
        for (x, y) in [(7, 2), (8, 2), (7, 3), (8, 3), (7, 4), (8, 4)]: px(img, x, y, '#bbbbbb')
        for y in range(5, 14):
            half = 2 + min(3, y - 5) if y < 9 else 5 - max(0, y - 11)
            for x in range(8 - half, 8 + half): px(img, x, y, color)
        px(img, 6, 8, (255, 255, 255, 255))
    return f


def blob(c1, c2=None):
    def f(img):
        c2_ = c2 or shade(rgb(c1), 0.7)
        for y in range(4, 13):
            for x in range(4, 13):
                d = (x - 8) ** 2 + (y - 8) ** 2
                if d < 20: px(img, x, y, c1 if d < 11 else c2_)
        px(img, 6, 6, (255, 255, 255, 255))
    return f


def scroll(c='#e8d8a8'):
    def f(img):
        for y in range(3, 13):
            for x in range(4, 12): px(img, x, y, c)
        for x in range(3, 13): px(img, x, 3, '#a07040'); px(img, x, 12, '#a07040')
        for y in (5, 7, 9): 
            for x in range(5, 11): px(img, x, y, '#7a5a30')
    return f


def card():
    def f(img):
        for y in range(3, 13):
            for x in range(1, 15): px(img, x, y, '#c9a227' if (x in (1, 14) or y in (3, 12)) else '#f0e0a0')
        for x in range(3, 7):
            for y in range(5, 9): px(img, x, y, '#6a4a2a')
        for x in range(8, 13): px(img, x, 6, '#7a5a30'); px(img, x, 8, '#7a5a30'); px(img, x, 10, '#7a5a30')
    return f


def leg(c):
    def f(img):
        for (x, y) in [(5, 10), (6, 9), (7, 8), (8, 7), (9, 6), (10, 5)]: px(img, x, y, c); px(img, x + 1, y, c)
        for (x, y) in [(11, 4), (12, 4), (12, 5), (11, 3)]: px(img, x, y, '#f0e6d2')
    return f


def cabbage_icon():
    def f(img):
        for y in range(3, 14):
            for x in range(3, 14):
                d = (x - 8) ** 2 + (y - 8) ** 2
                if d < 26: px(img, x, y, '#5fb04a' if d > 12 else '#a8e07a')
        for i in range(5, 12): px(img, i, 8, '#3e8a30')
    return f


def bottle():
    def f(img):
        for (x, y) in [(7, 2), (8, 2), (7, 3), (8, 3), (7, 4), (8, 4)]: px(img, x, y, '#a8c8a8')
        for y in range(5, 14):
            for x in range(5, 11): px(img, x, y, '#d8ecd8' if y < 8 else '#e8e8f8')
        px(img, 6, 6, '#ffffff')
    return f


ICONS = {
    'goblin_ear': blob('#5a9a3a'), 'kobold_fang': leg('#e0d8b0'), 'orc_tusk': leg('#efe8d0'), 'lizard_scale': blob('#2a9d8f', '#1d6f65'),
    'bone_dust': blob('#e8e4d0', '#b8b4a0'), 'magic_crystal': blob('#7a5af0', '#4a2ac0'), 'toad_leg': leg('#6b8e23'), 'fried_toad_leg': leg('#c98a3a'),
    'cabbage': cabbage_icon(), 'health_potion': potion('#d83030'), 'greater_health_potion': potion('#ff4a6a'), 'mana_potion': potion('#3060e0'),
    'greater_mana_potion': potion('#40a0ff'), 'return_scroll': scroll(), 'adventurer_card': card(), 'sake_bottle': bottle(),
    'adventurer_sword': sword('#b8b8c0', '#6a4a2a'), 'goblin_hatchet': axe('#8a8a90', '#6a4a2a'), 'knight_longsword': sword('#d8d8e0', '#8a6a2a'),
    'crusader_blade': sword('#9fd0ff', '#d4af37', '#ff4040'), 'gram_sword': sword('#e8c8ff', '#d4af37', '#ff3030', True), 'hunter_bow': bow('#8a5a2a'),
    'apprentice_staff': staff('#7a5a30', '#4a90e0'), 'archmage_staff': staff('#4a2a6a', '#e04aff'),
    'iron_ring': ring('#aaaaaa', '#888888'), 'mana_ring': ring('#3a70ff'), 'lucky_ring': ring('#40d060'), 'swift_ring': ring('#f0e040'),
    'guardian_ring': ring('#e04040'), 'flame_necklace': necklace('#ff7020'), 'eris_amulet': necklace('#90f0ff'),
}
for n, f in ICONS.items():
    icon(n, f)

# block textures
img = canvas(16, 16, rgb('#8a6a3a'))
for y in range(16):
    for x in range(16):
        img[y][x] = shade(rgb('#8a6a3a'), 0.9 + 0.1 * ((x * 3 + y * 5) % 3) / 2)
rect(img, 0, 0, 16, 1, rgb('#4a3216')); rect(img, 0, 15, 16, 1, rgb('#4a3216')); rect(img, 0, 0, 1, 16, rgb('#4a3216')); rect(img, 15, 0, 1, 16, rgb('#4a3216'))
for (x, y, ww, hh) in [(2, 2, 5, 6), (9, 3, 5, 5), (3, 9, 4, 5), (9, 10, 5, 4)]:
    rect(img, x, y, ww, hh, rgb('#f0e6c0'))
    rect(img, x + 1, y + 1, ww - 2, 1, rgb('#7a5a30'))
png(os.path.join(A, 'textures', 'block', 'quest_board_front.png'), 16, 16, img)
img = canvas(16, 16, rgb('#6a4a24'))
for y in range(16):
    for x in range(16):
        img[y][x] = shade(rgb('#6a4a24'), 0.9 + 0.1 * ((x + y * 2) % 3) / 2)
png(os.path.join(A, 'textures', 'block', 'quest_board_side.png'), 16, 16, img)

# ---------------------------------------------------------------- models
ITEMS_HANDHELD = ['adventurer_sword', 'goblin_hatchet', 'knight_longsword', 'crusader_blade', 'gram_sword', 'hunter_bow', 'apprentice_staff', 'archmage_staff']
for n in ICONS:
    js(os.path.join(A, 'models', 'item', n + '.json'), {'parent': 'minecraft:item/handheld' if n in ITEMS_HANDHELD else 'minecraft:item/generated', 'textures': {'layer0': 'konorpg:item/' + n}})
EGGS = ['goblin', 'kobold', 'orc', 'lizardman', 'skeleton_warrior', 'giant_toad', 'town_npc']
for e in EGGS:
    js(os.path.join(A, 'models', 'item', e + '_spawn_egg.json'), {'parent': 'minecraft:item/template_spawn_egg'})
js(os.path.join(A, 'models', 'block', 'quest_board.json'), {'parent': 'minecraft:block/orientable', 'textures': {'top': 'konorpg:block/quest_board_side', 'front': 'konorpg:block/quest_board_front', 'side': 'konorpg:block/quest_board_side'}})
js(os.path.join(A, 'models', 'item', 'quest_board.json'), {'parent': 'konorpg:block/quest_board'})
js(os.path.join(A, 'blockstates', 'quest_board.json'), {'variants': {
    'facing=north': {'model': 'konorpg:block/quest_board'},
    'facing=east': {'model': 'konorpg:block/quest_board', 'y': 90},
    'facing=south': {'model': 'konorpg:block/quest_board', 'y': 180},
    'facing=west': {'model': 'konorpg:block/quest_board', 'y': 270}}})

# ---------------------------------------------------------------- lang
NAMES = {
    'goblin_ear': ('Goblin Ear', 'Orelha de Goblin'), 'kobold_fang': ('Kobold Fang', 'Presa de Kobold'), 'orc_tusk': ('Orc Tusk', 'Presa de Orc'),
    'lizard_scale': ('Lizardman Scale', 'Escama de Homem-Lagarto'), 'bone_dust': ('Bone Dust', 'Pó de Osso'), 'magic_crystal': ('Magic Crystal', 'Cristal Mágico'),
    'toad_leg': ('Giant Toad Leg', 'Perna de Sapo Gigante'), 'fried_toad_leg': ('Fried Toad Leg', 'Perna de Sapo Frita'), 'cabbage': ('Flying Cabbage', 'Repolho Voador'),
    'health_potion': ('Health Potion', 'Poção de Vida'), 'greater_health_potion': ('Greater Health Potion', 'Poção de Vida Maior'),
    'mana_potion': ('Mana Potion', 'Poção de Mana'), 'greater_mana_potion': ('Greater Mana Potion', 'Poção de Mana Maior'),
    'return_scroll': ('Return Scroll', 'Pergaminho de Retorno'), 'adventurer_card': ('Adventurer Card', 'Cartão de Aventureiro'), 'sake_bottle': ('Axis Sake', 'Sake da Ordem Axis'),
    'adventurer_sword': ('Adventurer Sword', 'Espada de Aventureiro'), 'goblin_hatchet': ('Goblin Hatchet', 'Machadinha de Goblin'), 'knight_longsword': ('Knight Longsword', 'Espada Longa do Cavaleiro'),
    'crusader_blade': ('Crusader Blade', 'Lâmina do Cruzado'), 'gram_sword': ("Demon Sword 'Gram'", "Espada Demoníaca 'Gram'"), 'hunter_bow': ('Hunter Bow', 'Arco do Caçador'),
    'apprentice_staff': ('Apprentice Staff', 'Cajado de Aprendiz'), 'archmage_staff': ('Archmage Staff', 'Cajado do Arquimago'),
    'iron_ring': ('Iron Ring', 'Anel de Ferro'), 'mana_ring': ('Ring of Mana', 'Anel de Mana'), 'lucky_ring': ('Lucky Ring', 'Anel da Sorte'), 'swift_ring': ('Swift Ring', 'Anel Veloz'),
    'guardian_ring': ('Guardian Ring', 'Anel do Guardião'), 'flame_necklace': ('Flame Necklace', 'Colar de Chamas'), 'eris_amulet': ("Eris's Amulet", 'Amuleto de Eris'),
    'quest_board': ('Quest Board', 'Mural de Quests'),
}
ENT = {'goblin': ('Goblin', 'Goblin'), 'kobold': ('Kobold', 'Kobold'), 'orc': ('Orc', 'Orc'), 'lizardman': ('Lizardman', 'Homem-Lagarto'),
       'skeleton_warrior': ('Skeleton Warrior', 'Guerreiro Esqueleto'), 'giant_toad': ('Giant Toad', 'Sapo Gigante'), 'town_npc': ('Townsperson', 'Morador'), 'companion': ('Companion', 'Companheiro')}
KEYS = {'key.konorpg.hub': ('Open Adventurer Hub', 'Abrir Painel do Aventureiro'), 'key.konorpg.hud': ('Toggle Progress HUD', 'Alternar HUD de Progresso'),
        'key.konorpg.skill1': ('Skill 1', 'Habilidade 1'), 'key.konorpg.skill2': ('Skill 2', 'Habilidade 2'), 'key.konorpg.skill3': ('Skill 3', 'Habilidade 3'),
        'key.konorpg.skill4': ('Skill 4', 'Habilidade 4'), 'key.categories.konorpg': ('KonoRPG', 'KonoRPG'), 'itemGroup.konorpg': ('KonoRPG', 'KonoRPG')}
for idx, lang in enumerate(('en_us', 'pt_br')):
    d = {}
    for k, v in NAMES.items():
        d[('block.konorpg.' if k == 'quest_board' else 'item.konorpg.') + k] = v[idx]
    for k, v in ENT.items():
        d['entity.konorpg.' + k] = v[idx]
    for e in EGGS:
        d['item.konorpg.' + e + '_spawn_egg'] = (ENT[e][idx] + (' Spawn Egg' if idx == 0 else ' (Ovo de Invocação)'))
    for k, v in KEYS.items():
        d[k] = v[idx]
    js(os.path.join(A, 'lang', lang + '.json'), d)

# ---------------------------------------------------------------- data: loot, recipes, spawns
def drop_pool(item, chance, lo=1, hi=1):
    entry = {'type': 'minecraft:item', 'name': item, 'conditions': [{'condition': 'minecraft:random_chance', 'chance': chance}]}
    if hi > 1:
        entry['functions'] = [{'function': 'minecraft:set_count', 'count': {'type': 'minecraft:uniform', 'min': lo, 'max': hi}}]
    return {'rolls': 1, 'entries': [entry]}


LOOT = {
    'goblin': [('konorpg:goblin_ear', 0.75, 1, 2), ('konorpg:magic_crystal', 0.05, 1, 1), ('minecraft:iron_nugget', 0.4, 1, 3)],
    'kobold': [('konorpg:kobold_fang', 0.75, 1, 2), ('minecraft:stick', 0.5, 1, 2)],
    'orc': [('konorpg:orc_tusk', 0.65, 1, 2), ('minecraft:iron_ingot', 0.3, 1, 2), ('konorpg:health_potion', 0.1, 1, 1)],
    'lizardman': [('konorpg:lizard_scale', 0.75, 1, 3), ('konorpg:magic_crystal', 0.06, 1, 1)],
    'skeleton_warrior': [('konorpg:bone_dust', 0.8, 1, 2), ('minecraft:bone', 0.6, 1, 2), ('konorpg:mana_potion', 0.08, 1, 1)],
    'giant_toad': [('konorpg:toad_leg', 1.0, 1, 2), ('konorpg:magic_crystal', 0.04, 1, 1)],
}
for e, drops in LOOT.items():
    js(os.path.join(D, 'loot_table', 'entities', e + '.json'), {'type': 'minecraft:entity', 'pools': [drop_pool(*x) for x in drops]})
js(os.path.join(D, 'loot_table', 'blocks', 'quest_board.json'), {'type': 'minecraft:block', 'pools': [{'rolls': 1, 'entries': [{'type': 'minecraft:item', 'name': 'konorpg:quest_board'}], 'conditions': [{'condition': 'minecraft:survives_explosion'}]}]})
js(os.path.join(D, 'recipe', 'fried_toad_leg.json'), {'type': 'minecraft:smelting', 'ingredient': {'item': 'konorpg:toad_leg'}, 'result': {'id': 'konorpg:fried_toad_leg'}, 'experience': 0.2, 'cookingtime': 200})
js(os.path.join(D, 'recipe', 'fried_toad_leg_smoking.json'), {'type': 'minecraft:smoking', 'ingredient': {'item': 'konorpg:toad_leg'}, 'result': {'id': 'konorpg:fried_toad_leg'}, 'experience': 0.2, 'cookingtime': 100})
js(os.path.join(D, 'neoforge', 'biome_modifier', 'spawns.json'), {'type': 'neoforge:add_spawns', 'biomes': '#minecraft:is_overworld', 'spawners': [
    {'type': 'konorpg:goblin', 'weight': 5, 'minCount': 2, 'maxCount': 3}, {'type': 'konorpg:kobold', 'weight': 4, 'minCount': 2, 'maxCount': 3},
    {'type': 'konorpg:giant_toad', 'weight': 6, 'minCount': 1, 'maxCount': 3}, {'type': 'konorpg:orc', 'weight': 1, 'minCount': 1, 'maxCount': 2},
    {'type': 'konorpg:lizardman', 'weight': 2, 'minCount': 1, 'maxCount': 2}, {'type': 'konorpg:skeleton_warrior', 'weight': 2, 'minCount': 1, 'maxCount': 2}]})

# ---------------------------------------------------------------- data: quests
def quest(id, **kw):
    q = {'name': id, 'description': '', 'type': 'kill', 'amount': 1, 'xp': 30, 'money': 50, 'difficulty': 1, 'minRank': 'F', 'category': 'board', 'repeatable': False}
    q.update(kw)
    js(os.path.join(D, 'konorpg_quests', id + '.json'), q)


quest('main_01_guild', name='Capítulo 1: Bem-vindo à Guilda', description='Você acordou em um mundo de fantasia. Vá até a Guilda dos Aventureiros (prédio ao norte da praça), fale com a recepcionista Luna e registre-se.', location='Guilda de Axel', type='event', target='register_guild', xp=50, money=100, category='main', unlocks=['main_02_class'], humor='Dica: uma taxa de inscrição existe. Sim, até no outro mundo tem burocracia.', reputation={'guild': 2})
quest('main_02_class', name='Capítulo 1: Escolha sua Classe', description='Abra o painel (tecla K) > GUILDA e escolha sua classe. Depois aprenda uma habilidade na aba SKILLS.', location='Guilda de Axel', type='event', target='choose_class', xp=60, money=150, category='main', unlocks=['main_03_toads'], reputation={'guild': 2})
quest('main_03_toads', name='Capítulo 1: Sapos Gigantes', description='Os sapos gigantes estão engolindo aventureiros nas planícies. Derrote 3 deles fora da cidade.', location='Planícies fora de Axel', type='kill', target='konorpg:giant_toad', amount=3, xp=120, money=300, category='main', unlocks=['main_04_goblins'], humor='Aviso: sapos gostam de engolir coisas. Principalmente deusas.', reputation={'guild': 3, 'city': 2})
quest('main_04_goblins', name='Capítulo 1: Praga de Goblins', description='Goblins estão roubando as colheitas. Derrote 6 goblins.', location='Arredores de Axel', type='kill', target='konorpg:goblin', amount=6, xp=160, money=400, category='main', unlocks=['main_05_gate'], difficulty=2, reputation={'guild': 3, 'city': 3})
quest('main_05_gate', name='Capítulo 1: Os Portões', description='Visite o portão sul de Axel, a saída da cidade, e converse com os guardas sobre os rumores.', location='Portão sul de Axel', type='explore', target='gate', xp=80, money=200, category='main', unlocks=['main_06_skeletons'], reputation={'city': 3})
quest('main_06_skeletons', name='Capítulo 1: Guerreiros Esqueleto', description='Esqueletos armados foram vistos à noite. Derrote 5 Guerreiros Esqueleto.', location='Fora da cidade (à noite)', type='kill', target='konorpg:skeleton_warrior', amount=5, xp=220, money=600, category='main', unlocks=['main_07_rank'], difficulty=3, reputation={'guild': 4})
quest('main_07_rank', name='Capítulo 1: Aventureiro de Verdade', description='Faça quests no mural até alcançar o Rank D na Guilda. (Capítulo 2: dungeons e chefes - EM BREVE)', location='Guilda de Axel', type='event', target='rank:D', xp=400, money=1500, category='main', difficulty=3, minLevel=5, reputation={'guild': 10, 'city': 5})

quest('b_toads', name='Caça aos Sapos Gigantes', description='Derrote 3 sapos gigantes.', location='Planícies', target='konorpg:giant_toad', amount=3, xp=90, money=180, repeatable=True, reputation={'guild': 2}, humor='Recompensa: dinheiro e uma lição sobre sapos.')
quest('b_goblins', name='Controle de Goblins', description='Derrote 6 goblins.', location='Arredores', target='konorpg:goblin', amount=6, xp=110, money=220, repeatable=True, reputation={'guild': 2})
quest('b_kobolds', name='Kobolds Chatos', description='Derrote 6 kobolds.', location='Arredores', target='konorpg:kobold', amount=6, xp=120, money=240, repeatable=True, minRank='E', difficulty=2, reputation={'guild': 2})
quest('b_lizardmen', name='Homens-Lagarto', description='Derrote 5 homens-lagarto.', location='Pântanos e rios', target='konorpg:lizardman', amount=5, xp=200, money=380, repeatable=True, minRank='E', difficulty=2, reputation={'guild': 3})
quest('b_skeletons', name='Limpeza de Esqueletos', description='Derrote 6 guerreiros esqueleto.', location='Campos à noite', target='konorpg:skeleton_warrior', amount=6, xp=220, money=420, repeatable=True, minRank='E', difficulty=3, reputation={'guild': 3})
quest('b_orcs', name='Ameaça Orc', description='Derrote 4 orcs.', location='Florestas', target='konorpg:orc', amount=4, xp=300, money=600, repeatable=True, minRank='D', difficulty=4, reputation={'guild': 4, 'city': 2}, humor='Orcs fedem. A recompensa compensa. Mais ou menos.')
quest('b_goblin_ears', name='Orelhas de Goblin', description='A Guilda paga por orelhas de goblin. Entregue 8.', location='Qualquer lugar', type='collect', target='konorpg:goblin_ear', amount=8, xp=80, money=200, repeatable=True, reputation={'guild': 1, 'merchants': 1}, humor='Sim, é nojento. Sim, paga bem.')
quest('b_toad_legs', name='Cardápio de Sapo', description='A taverna quer pernas de sapo gigante. Entregue 5.', location='Qualquer lugar', type='collect', target='konorpg:toad_leg', amount=5, xp=70, money=150, repeatable=True, reputation={'merchants': 2}, humor='Dizem que parece frango. Ninguém confirmou.')
quest('b_crystals', name='Cristais Mágicos', description='Entregue 2 cristais mágicos para o Mago da cidade.', location='Qualquer lugar', type='collect', target='konorpg:magic_crystal', amount=2, xp=250, money=700, repeatable=True, minRank='E', difficulty=3, reputation={'guild': 2, 'merchants': 3})
quest('b_bones', name='Pó de Osso', description='Entregue 6 porções de pó de osso para a alquimista.', location='Qualquer lugar', type='collect', target='konorpg:bone_dust', amount=6, xp=100, money=260, repeatable=True, reputation={'merchants': 2})
quest('c_aqua_sake', name='Aqua e o Sake Sumido', description='Aqua jura que não bebeu o sake. Traga 2 garrafas de sake da loja para provar a inocência dela.', location='Taverna de Axel', type='collect', target='konorpg:sake_bottle', amount=2, xp=100, money=100, category='special', repeatable=False, require={'companion': 'aqua'}, humor='Recompensa: gratidão (duvidosa) da deusa.', reputation={'axis': 5})

# ---------------------------------------------------------------- data: dialogues
def node(text, choices):
    return {'text': text, 'choices': choices}


def ch(text, next='', effects=None, **kw):
    c = {'text': text, 'next': next}
    if effects: c['effects'] = effects
    c.update(kw)
    return c


def act(a):
    return {'type': 'action', 'action': a}


js(os.path.join(D, 'konorpg_dialogues', 'luna.json'), {'speaker': 'Luna', 'start': 'back', 'starts': [{'require': {'registered': False}, 'node': 'new'}], 'nodes': {
    'new': node('Bem-vindo à Guilda dos Aventureiros! Para se registrar, a taxa de inscrição é de {fee} Eris. Você tem {money} Eris.', [
        ch('Quero me registrar.', 'done', [act('register')], next_fail='poor'), ch('Só estou olhando.')]),
    'done': node('Pronto! Aqui está o seu Cartão de Aventureiro. Abra o painel (tecla K) > GUILDA para escolher a classe e confira o mural de quests!', [ch('Obrigado!')]),
    'poor': node('Hmm... a taxa é {fee} Eris e você tem só {money}. Aventureiros sem dinheiro? Comum por aqui. Volte quando tiver.', [ch('Entendi.')]),
    'back': node(['Olá, {player}! Rank {rank}, nível {level}. Como posso ajudar?', 'Bem-vindo de volta! Algum monstro precisando de uma lição?'], [
        ch('Abrir o painel da Guilda (classe, cartão)', '', [act('open_guild')]), ch('Ver o mural de quests', '', [act('open_board')]),
        ch('Dicas de aventureiro', 'tips'), ch('Tchau.')]),
    'tips': node('Dicas: monstros dão XP e Eris. Itens deles valem dinheiro nas lojas. Quests sobem seu rank. E nunca, NUNCA deixe a deusa beber antes de uma luta.', [ch('Voltar', 'back')])}})

js(os.path.join(D, 'konorpg_dialogues', 'shopkeeper.json'), {'speaker': 'Lojista', 'start': 'greet', 'nodes': {
    'greet': node(['Bem-vindo! Veja minhas mercadorias.', 'Olá, aventureiro! Eris na mão, itens no inventário.'], [
        ch('Ver a loja', '', [act('open_shop')]), ch('Alguma novidade?', 'news'), ch('Tchau.')]),
    'news': node(['Dizem que sapos gigantes andam engolindo aventureiros nas planícies. Principalmente os que ficam gritando.', 'Cuidado com os goblins à noite. E com deusas bêbadas, a qualquer hora.', 'Reputação com os comerciantes dá desconto, sabia? Vendendo itens de monstro você ganha a nossa confiança.'], [ch('Voltar', 'greet')])}})

js(os.path.join(D, 'konorpg_dialogues', 'blacksmith.json'), {'speaker': 'Ferreiro', 'start': 'greet', 'nodes': {
    'greet': node('Bom trabalho exige bom aço. O que precisa?', [ch('Ver a loja', '', [act('open_shop')]), ch('Reparar/Reforçar equipamentos (EM BREVE)', 'soon'), ch('Tchau.')]),
    'soon': node('Estou montando a bancada. Em breve eu reparo e reforço suas armas. Volte depois!', [ch('Voltar', 'greet')])}})

js(os.path.join(D, 'konorpg_dialogues', 'alchemist.json'), {'speaker': 'Alquimista', 'start': 'greet', 'nodes': {
    'greet': node('Poções! Efeitos colaterais não inclusos.', [ch('Ver a loja', '', [act('open_shop')]), ch('Criar poções especiais (EM BREVE)', 'soon'), ch('Tchau.')]),
    'soon': node('Meu laboratório ainda está em obras. Em breve, receitas com ingredientes de monstros!', [ch('Voltar', 'greet')])}})

js(os.path.join(D, 'konorpg_dialogues', 'innkeeper.json'), {'speaker': 'Dona da Pousada', 'start': 'greet', 'nodes': {
    'greet': node('Bem-vindo à pousada! Uma noite de descanso por 50 Eris cura tudo e salva seu ponto de retorno.', [
        ch('Descansar (50 Eris)', 'rested', [act('rest')], next_fail='greet'), ch('Só salvar meu ponto de retorno (grátis)', 'saved', [act('set_respawn')]), ch('Tchau.')]),
    'rested': node('Dormiu bem? Seus ferimentos e sua mana foram restaurados.', [ch('Obrigado!')]),
    'saved': node('Pronto, se você cair em batalha, acorda aqui. Se a deusa não ajudar antes.', [ch('Valeu.')])}})

js(os.path.join(D, 'konorpg_dialogues', 'tavern.json'), {'speaker': 'Taverneiro', 'start': 'greet', 'nodes': {
    'greet': node(['O que vai ser? Pernas de sapo fritas é o prato do dia!', 'Boa noite! Aqui se bebe, se come e se fofoca.'], [
        ch('Ver o cardápio', '', [act('open_shop')]), ch('Ouvir rumores', 'rumor'), ch('Tchau.')]),
    'rumor': node(['Ouvi dizer que existe uma caverna de goblins além dos portões... e que alguém muito poderoso a vigia. (EM BREVE)', 'Uma deusa de cabelo azul esteve aqui outro dia e me deixou a conta. Mas isso é papo de bêbado.', 'Dizem que um general demoníaco está reunindo exércitos. Espero que ele seja ruim em logística.'], [ch('Voltar', 'greet')])}})

js(os.path.join(D, 'konorpg_dialogues', 'guard.json'), {'speaker': 'Guarda', 'start': 'greet', 'nodes': {
    'greet': node(['Mantenha a ordem na cidade, aventureiro.', 'Axel é segura. Quase sempre. Não bata nos moradores.', 'Os monstros lá fora estão ficando mais ousados. Cuide-se.'], [ch('Rumores?', 'rumor'), ch('Tchau.')]),
    'rumor': node('Ultimamente, mortos-vivos têm surgido à noite. Um padre faria bem... ou uma deusa. Se ela estiver sóbria.', [ch('Entendi.')])}})

js(os.path.join(D, 'konorpg_dialogues', 'villager.json'), {'speaker': 'Morador', 'start': 'greet', 'nodes': {
    'greet': node(['Bom dia! Você é novo por aqui? Cuidado com os sapos.', 'Eu queria ser aventureiro, mas tenho medo de sapos. Muito medo.', 'Sabia que repolhos voam por aqui? Eu não acreditei. Agora acredito.', 'Dizem que a Guilda paga bem por orelhas de goblin. Eu prefiro não perguntar para que.', 'Minha vaca fugiu. Disseram que foi um goblin. Mas não tenho vaca.'], [
        ch('Tchau.'), ch('Alguma dica?', 'tip')]),
    'tip': node('Dica: venda os itens de monstros nas lojas. E mantenha sempre uma poção de vida no bolso.', [ch('Valeu!')])}})

js(os.path.join(D, 'konorpg_dialogues', 'aqua.json'), {'speaker': 'Aqua', 'start': 'talk', 'nodes': {
    'talk': node(['Sim? Fale com a deusa. Rápido, estou ocupada. (Não estou.)', 'Oi! Veio me pagar uma bebida? Não? Que droga.'], [
        ch('Aqua, me cure!', '', [act('companion_heal'), {'type': 'friendship', 'id': 'aqua', 'amount': 1}]),
        ch('Como você está?', 'how'),
        ch('Me fale da Ordem Axis.', 'axis', require={'friendship': {'id': 'aqua', 'min': 2}}),
        ch('Fique aqui / Siga-me', '', [act('companion_stay')]),
        ch('Preciso que você vá embora.', 'refuse'),
        ch('Tchau.')]),
    'how': node(['Eu? Maravilhosa, claro! Só estou com sede... e com fome... e com saudade do céu.', 'Estou ótima! Mas se aparecer um sapo gigante, eu me escondo atrás de você. Combinado?'], [ch('Voltar', 'talk')]),
    'axis': node('A Ordem Axis me adora! É a maior religião do mundo... ou será que é a pior reputação do mundo? Ninguém sabe. Mas eles me dão presentes, às vezes.', [ch('Voltar', 'talk', [{'type': 'rep', 'faction': 'axis', 'amount': 1}])]),
    'refuse': node('Como é?! Eu sou uma DEUSA! Não vou a lugar nenhum! Você me trouxe para este mundo, agora aguenta!', [ch('Desculpe...', 'talk', [{'type': 'friendship', 'id': 'aqua', 'amount': -1}])])}})

# ---------------------------------------------------------------- data: shops + economy
def shop(id, name, items):
    js(os.path.join(D, 'konorpg_shops', id + '.json'), {'name': name, 'sell': True, 'buy': [{'item': i, 'price': p} for i, p in items]})


shop('weapons', 'Armas do Bork', [('konorpg:adventurer_sword', 250), ('konorpg:goblin_hatchet', 400), ('konorpg:knight_longsword', 1200), ('konorpg:crusader_blade', 4000), ('konorpg:hunter_bow', 900), ('minecraft:arrow', 4)])
shop('armor', 'Armaduras da Greta', [('minecraft:leather_helmet', 60), ('minecraft:leather_chestplate', 100), ('minecraft:leather_leggings', 90), ('minecraft:leather_boots', 50),
                                       ('minecraft:chainmail_chestplate', 500), ('minecraft:iron_helmet', 300), ('minecraft:iron_chestplate', 600), ('minecraft:iron_leggings', 500), ('minecraft:iron_boots', 280), ('minecraft:shield', 220)])
shop('magic', 'Magia do Wiz', [('konorpg:apprentice_staff', 500), ('konorpg:archmage_staff', 6000), ('konorpg:mana_potion', 90), ('konorpg:greater_mana_potion', 280), ('konorpg:mana_ring', 1500), ('konorpg:flame_necklace', 2500), ('konorpg:lucky_ring', 2200)])
shop('items', 'Itens do Tomás', [('konorpg:health_potion', 80), ('konorpg:greater_health_potion', 250), ('konorpg:return_scroll', 150), ('minecraft:bread', 10), ('minecraft:torch', 3), ('konorpg:iron_ring', 700), ('konorpg:swift_ring', 1500), ('konorpg:guardian_ring', 1500)])
shop('smith', 'Forja do Grom', [('minecraft:iron_ingot', 40), ('minecraft:coal', 6), ('minecraft:iron_sword', 220), ('minecraft:iron_pickaxe', 220), ('minecraft:iron_axe', 220)])
shop('alchemy', 'Alquimia da Lyra', [('konorpg:health_potion', 75), ('konorpg:mana_potion', 85), ('konorpg:greater_health_potion', 235), ('konorpg:greater_mana_potion', 265), ('konorpg:magic_crystal', 200)])
shop('tavern', 'Taverna do Joe', [('konorpg:fried_toad_leg', 30), ('minecraft:cooked_beef', 20), ('minecraft:bread', 8), ('konorpg:sake_bottle', 120)])
js(os.path.join(D, 'konorpg_economy', 'sell_prices.json'), {
    'konorpg:goblin_ear': 12, 'konorpg:kobold_fang': 10, 'konorpg:orc_tusk': 35, 'konorpg:lizard_scale': 25, 'konorpg:bone_dust': 8, 'konorpg:magic_crystal': 80,
    'konorpg:toad_leg': 8, 'konorpg:fried_toad_leg': 18, 'konorpg:cabbage': 40, 'konorpg:sake_bottle': 40,
    'minecraft:iron_ingot': 15, 'minecraft:gold_ingot': 30, 'minecraft:diamond': 120, 'minecraft:emerald': 50, 'minecraft:bone': 4, 'minecraft:string': 3, 'minecraft:gunpowder': 8,
    'minecraft:rotten_flesh': 1, 'minecraft:iron_nugget': 2, 'minecraft:coal': 3})
print('assets generated')
