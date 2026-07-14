#!/usr/bin/env python3
"""Generate owned reference-matched item models, material textures, and particles.

The source of truth is the palette and geometry below. Generated resources are
checked into the repository so the game never needs this script at runtime.
"""

from __future__ import annotations

import json
import struct
import zlib
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
TEXTURE_DIR = ROOT / "src/main/resources/assets/guns/textures/item"
MODEL_DIR = ROOT / "src/main/resources/assets/guns/models/item"
PARTICLE_TEXTURE_DIR = ROOT / "src/main/resources/assets/guns/textures/particle"
PARTICLE_DEFINITION_DIR = ROOT / "src/main/resources/assets/guns/particles"
ITEM_TEXTURE_SIZE = 64
MATERIAL_TILE_SIZE = 16

PATCHES = {
    "dark": [0, 0, 4, 4],
    "primary": [4, 0, 8, 4],
    "accent": [8, 0, 12, 4],
    "light": [12, 0, 16, 4],
    "metal": [0, 4, 4, 8],
    "secondary": [4, 4, 8, 8],
    "glow": [8, 4, 12, 8],
    "shadow": [12, 4, 16, 8],
}

WEAPONS = {
    "sniper_rifle",
    "shotgun",
    "grenade_launcher",
    "smg",
    "flamethrower",
    "railgun",
}


def color(hex_value: str) -> tuple[int, int, int, int]:
    hex_value = hex_value.lstrip("#")
    return tuple(int(hex_value[index:index + 2], 16) for index in range(0, 6, 2)) + (255,)


PALETTES = {
    "sniper_rifle": {
        "dark": "#16253B", "primary": "#1768D5", "accent": "#F6AA16", "light": "#C8F6FF",
        "metal": "#E4E9ED", "secondary": "#71401F", "glow": "#22DDF4", "shadow": "#091322",
    },
    "shotgun": {
        "dark": "#17253D", "primary": "#1767D2", "accent": "#F25A19", "light": "#F7B328",
        "metal": "#D8E1E7", "secondary": "#6B3D1D", "glow": "#FF8B2B", "shadow": "#0B1425",
    },
    "grenade_launcher": {
        "dark": "#251642", "primary": "#7132C5", "accent": "#78D925", "light": "#FF7817",
        "metal": "#D9E1E8", "secondary": "#4A247C", "glow": "#AAF238", "shadow": "#110822",
    },
    "smg": {
        "dark": "#153160", "primary": "#1861CF", "accent": "#F47A19", "light": "#F5B52A",
        "metal": "#D9E6F0", "secondary": "#D95B14", "glow": "#49B4FF", "shadow": "#091830",
    },
    "flamethrower": {
        "dark": "#45251B", "primary": "#E95E14", "accent": "#FFC022", "light": "#F4F4EF",
        "metal": "#D7E0E5", "secondary": "#1479D1", "glow": "#4FCBFF", "shadow": "#21100B",
    },
    "railgun": {
        "dark": "#17255F", "primary": "#3E4AC8", "accent": "#22D4D1", "light": "#F28A13",
        "metal": "#69B82D", "secondary": "#245F9B", "glow": "#75F0E2", "shadow": "#08102F",
    },
    "rifle_round": {
        "dark": "#173B71", "primary": "#2079E5", "accent": "#F2B52B", "light": "#D8F4FF",
        "metal": "#C9D7E2", "secondary": "#A36B1D", "glow": "#53C9FF", "shadow": "#091A35",
    },
    "shotgun_shell": {
        "dark": "#5B1918", "primary": "#D93622", "accent": "#F4A719", "light": "#FEE5AA",
        "metal": "#E1E7EC", "secondary": "#9A2418", "glow": "#FF6B36", "shadow": "#260B0A",
    },
    "grenade_round": {
        "dark": "#204A24", "primary": "#4E9E46", "accent": "#E2C62F", "light": "#B9E86D",
        "metal": "#CFDBD7", "secondary": "#285F32", "glow": "#A9EE5A", "shadow": "#0F2713",
    },
    "fuel_cell": {
        "dark": "#174C59", "primary": "#28BFD0", "accent": "#E6F6F2", "light": "#C6FFFF",
        "metal": "#D6E7E8", "secondary": "#187A8B", "glow": "#70FFFF", "shadow": "#0A262D",
    },
    "railgun_cell": {
        "dark": "#41206E", "primary": "#7A43D5", "accent": "#E2D6FF", "light": "#C88CFF",
        "metal": "#D9E2EA", "secondary": "#4F2A9B", "glow": "#B75CFF", "shadow": "#200B40",
    },
    "upgrade_template": {
        "dark": "#46301A", "primary": "#B97B2D", "accent": "#F7D471", "light": "#FFF2B7",
        "metal": "#E2E1D4", "secondary": "#7B4D1C", "glow": "#FFC84A", "shadow": "#221509",
    },
    "precision_barrel": {
        "dark": "#173B62", "primary": "#2B78C7", "accent": "#F3B427", "light": "#D9F0FF",
        "metal": "#C9D9E6", "secondary": "#1A4B86", "glow": "#5DCBFF", "shadow": "#091C31",
    },
    "cooling_system": {
        "dark": "#144B58", "primary": "#2296AF", "accent": "#D8F4F5", "light": "#87FFFF",
        "metal": "#D4E4E9", "secondary": "#166D80", "glow": "#4AE8F6", "shadow": "#08242B",
    },
    "reinforced_receiver": {
        "dark": "#5A2316", "primary": "#C84B20", "accent": "#F6A61C", "light": "#FFE0A0",
        "metal": "#DBE1E5", "secondary": "#84301A", "glow": "#FF7832", "shadow": "#290C08",
    },
}

# 16x8 reference silhouettes occupy the bottom half of the 64x64 texture. Each
# logical pixel is rendered as a shaded 4x4 pixel-art cell. The top half is the
# high-detail material atlas consumed by the custom cuboid faces.
GLYPHS = {
    "sniper_rifle": [
        "______gg________", "_____gllg_______", "ppppppppppppa___", "ppppppppppppp___",
        "_____ss___s_____", "_________ss_____", "__________s_____", "________________",
    ],
    "shotgun": [
        "________________", "pppppppppppa____", "pppppppppppaa___", "____ss____ss____",
        "___________ss___", "____________s___", "________________", "________________",
    ],
    "grenade_launcher": [
        "________________", "__aaaaaaaaaa____", "_appppppppppa___", "_appppppppppa___",
        "_____ss___ss____", "___________s____", "________________", "________________",
    ],
    "smg": [
        "________________", "___ppppppppaa___", "__pppppppppaa___", "_____ss___ss____",
        "___________s____", "________________", "________________", "________________",
    ],
    "flamethrower": [
        "________________", "__llllppppaaaa__", "__llllppppaaaa__", "_____pppp__ss___",
        "______ggg___s___", "______ggg_______", "________________", "________________",
    ],
    "railgun": [
        "________________", "__ppggggggpp____", "__ppggggggpp____", "__ppggggggpp____",
        "______ss___s____", "________________", "________________", "________________",
    ],
    "rifle_round": [
        "_______l________", "______lll_______", "______ppp_______", "______ppp_______",
        "______aaa_______", "______aaa_______", "________________", "________________",
    ],
    "shotgun_shell": [
        "_______l________", "______ppp_______", "______ppp_______", "______ppp_______",
        "______aaa_______", "______aaa_______", "________________", "________________",
    ],
    "grenade_round": [
        "______aaa_______", "_____apppa______", "_____ppppp______", "_____ppppp______",
        "______ppp_______", "_______s________", "________________", "________________",
    ],
    "fuel_cell": [
        "______llll______", "______pppp______", "______pggp______", "______pggp______",
        "______pppp______", "______aaaa______", "________________", "________________",
    ],
    "railgun_cell": [
        "______llll______", "______pppp______", "______pggp______", "______pggp______",
        "______pppp______", "______aaaa______", "________________", "________________",
    ],
    "upgrade_template": [
        "_____llllllll___", "_____lppppppl___", "_____lpaagpl____", "_____lppppppl___",
        "_____lppppppl___", "_____llllllll___", "________________", "________________",
    ],
    "precision_barrel": [
        "________________", "__mmmmpppppp____", "__mmmmpppppp____", "______aaaa______",
        "________________", "________________", "________________", "________________",
    ],
    "cooling_system": [
        "______llll______", "_____lppppl_____", "_____pggggp_____", "_____pggggp_____",
        "_____lppppl_____", "______llll______", "________________", "________________",
    ],
    "reinforced_receiver": [
        "________________", "_____mmmmmm_____", "____mppppppm____", "____mppppppm____",
        "_____aaaaaa_____", "________________", "________________", "________________",
    ],
}


def shade(value: tuple[int, int, int, int], factor: float) -> tuple[int, int, int, int]:
    return tuple(max(0, min(255, round(channel * factor))) for channel in value[:3]) + (value[3],)


def with_alpha(value: tuple[int, int, int, int], alpha: int) -> tuple[int, int, int, int]:
    return value[:3] + (max(0, min(255, alpha)),)


def write_png(path: Path, pixels: list[list[tuple[int, int, int, int]]]) -> None:
    height = len(pixels)
    width = len(pixels[0])
    raw = b"".join(b"\x00" + b"".join(bytes(pixel) for pixel in row) for row in pixels)

    def chunk(kind: bytes, payload: bytes) -> bytes:
        return struct.pack(">I", len(payload)) + kind + payload + struct.pack(">I", zlib.crc32(kind + payload) & 0xFFFFFFFF)

    path.write_bytes(
        b"\x89PNG\r\n\x1a\n"
        + chunk(b"IHDR", struct.pack(">IIBBBBB", width, height, 8, 6, 0, 0, 0))
        + chunk(b"IDAT", zlib.compress(raw, 9))
        + chunk(b"IEND", b"")
    )


def texture(name: str) -> None:
    palette = {key: color(value) for key, value in PALETTES[name].items()}
    glyph_materials = {
        "d": "dark",
        "p": "primary",
        "a": "accent",
        "l": "light",
        "m": "metal",
        "s": "secondary",
        "g": "glow",
        "h": "shadow",
    }
    pixels = [[(0, 0, 0, 0) for _ in range(ITEM_TEXTURE_SIZE)] for _ in range(ITEM_TEXTURE_SIZE)]
    swatches = list(PATCHES)
    for swatch_index, material in enumerate(swatches):
        start_x = (swatch_index % 4) * MATERIAL_TILE_SIZE
        start_y = (swatch_index // 4) * MATERIAL_TILE_SIZE
        for local_y in range(MATERIAL_TILE_SIZE):
            for local_x in range(MATERIAL_TILE_SIZE):
                edge = min(local_x, local_y, MATERIAL_TILE_SIZE - 1 - local_x, MATERIAL_TILE_SIZE - 1 - local_y)
                if edge == 0:
                    pixel = shade(palette["shadow"], 0.58)
                elif local_y <= 2 or local_x <= 2:
                    pixel = shade(palette[material], 1.22 if edge == 1 else 1.12)
                elif local_y >= 13 or local_x >= 13:
                    pixel = shade(palette[material], 0.67 if edge == 1 else 0.78)
                else:
                    gradient = 1.12 - (local_x + local_y) * 0.012
                    dither = 0.07 if (local_x * 3 + local_y * 5 + swatch_index) % 13 == 0 else 0.0
                    pixel = shade(palette[material], gradient + dither)
                pixels[start_y + local_y][start_x + local_x] = pixel

        # Hand-placed pixel details survive model-face scaling and mirror the
        # reference sheet's rivets, panel recesses, metallic glints, and cores.
        if material == "primary" and name in WEAPONS:
            for local_x, local_y in ((6, 8), (7, 8), (10, 9), (11, 9), (5, 11), (9, 6)):
                pixels[start_y + local_y][start_x + local_x] = shade(palette["dark"], 0.82)
            for local_x in range(4, 11):
                pixels[start_y + 4][start_x + local_x] = shade(palette[material], 1.28)
        elif material == "primary":
            for local_y in range(4, 12):
                pixels[start_y + local_y][start_x + 4] = shade(palette["light"], 1.08)
                pixels[start_y + local_y][start_x + 11] = shade(palette["dark"], 0.76)
        elif material == "metal":
            for offset in range(4, 10):
                pixels[start_y + offset][start_x + 4] = shade(palette["light"], 1.22)
                pixels[start_y + 11][start_x + offset] = shade(palette["dark"], 0.72)
        elif material == "glow":
            for local_y in range(6, 10):
                for local_x in range(6, 10):
                    factor = 1.42 if local_x in (7, 8) or local_y in (7, 8) else 1.22
                    pixels[start_y + local_y][start_x + local_x] = shade(palette[material], factor)
        elif material == "accent":
            for local_x in range(4, 12):
                pixels[start_y + 5][start_x + local_x] = shade(palette[material], 1.30)
                pixels[start_y + 10][start_x + local_x] = shade(palette["dark"], 0.72)
        elif material == "secondary":
            for local_y in range(4, 12):
                pixels[start_y + local_y][start_x + 7] = shade(palette[material], 1.20)
                pixels[start_y + local_y][start_x + 10] = shade(palette["dark"], 0.78)

    for row_index, row in enumerate(GLYPHS[name]):
        assert len(row) == 16, f"{name} glyph row {row_index} must be 16 pixels"
        for column_index, material in enumerate(row):
            if material != "_":
                base = palette[glyph_materials[material]]
                for local_y in range(4):
                    for local_x in range(4):
                        left_empty = column_index == 0 or row[column_index - 1] == "_"
                        right_empty = column_index == 15 or row[column_index + 1] == "_"
                        above_empty = row_index == 0 or GLYPHS[name][row_index - 1][column_index] == "_"
                        below_empty = row_index == 7 or GLYPHS[name][row_index + 1][column_index] == "_"
                        outline = (
                            (left_empty and local_x == 0)
                            or (right_empty and local_x == 3)
                            or (above_empty and local_y == 0)
                            or (below_empty and local_y == 3)
                        )
                        if outline:
                            pixel = shade(palette["shadow"], 0.5)
                        else:
                            factor = 1.18 if local_x + local_y <= 2 else 0.76 if local_x + local_y >= 5 else 1.0
                            pixel = shade(base, factor)
                        pixels[32 + row_index * 4 + local_y][column_index * 4 + local_x] = pixel
    write_png(TEXTURE_DIR / f"{name}.png", pixels)


PARTICLE_FRAMES = {
    "tracer": 2,
    "muzzle_flash": 3,
    "impact_spark": 3,
    "flame_core": 4,
    "energy_arc": 3,
    "blast_wave": 3,
    "shockwave": 3,
    "gray_round": 2,
    "gray_range": 2,
    "black_round": 2,
    "flame_jet": 4,
    "white_beam": 3,
}


def set_pixel(pixels: list[list[tuple[int, int, int, int]]], x: int, y: int, value: tuple[int, int, int, int]) -> None:
    if 0 <= y < len(pixels) and 0 <= x < len(pixels[0]):
        pixels[y][x] = value


def particle_texture(kind: str, frame: int) -> list[list[tuple[int, int, int, int]]]:
    size = 16
    pixels = [[(0, 0, 0, 0) for _ in range(size)] for _ in range(size)]
    gold = color("#FFD447")
    orange = color("#FF7A18")
    white = color("#FFF7C7")
    cyan = color("#52F3FF")
    blue = color("#2878FF")
    violet = color("#866CFF")
    gray_dark = color("#4B5057")
    gray_mid = color("#9299A1")
    gray_light = color("#D8DCE0")
    black = color("#080A0D")
    charcoal = color("#20242A")
    ember = color("#B92E0E")
    beam_white = color("#FFFFFF")
    beam_edge = color("#DDE3EA")

    if kind == "tracer":
        radius = 3 - frame
        for y in range(size):
            for x in range(size):
                distance = abs(x - 7.5) + abs(y - 7.5)
                if distance <= radius + 0.5:
                    set_pixel(pixels, x, y, with_alpha(white if distance < 1.5 else gold, 255 - frame * 25))
                elif (x in (7, 8) or y in (7, 8)) and distance <= 5.5 - frame:
                    set_pixel(pixels, x, y, with_alpha(gold, 135))
    elif kind in ("gray_round", "black_round"):
        if kind == "gray_round":
            left = 5 + frame
            right = 10 + frame
            for y in range(6, 10):
                for x in range(left, right + 1):
                    border = x in (left, right) or y in (6, 9)
                    value = gray_dark if border else gray_light if y == 7 and x < right - 1 else gray_mid
                    set_pixel(pixels, x, y, value)
        else:
            for y in range(6, 10):
                for x in range(6 + frame, 10 + frame):
                    border = x in (6 + frame, 9 + frame) or y in (6, 9)
                    value = black if border else charcoal
                    set_pixel(pixels, x, y, value)
            set_pixel(pixels, 7 + frame, 6, gray_dark)
    elif kind == "gray_range":
        radius = 3.4 + frame * 0.55
        for y in range(size):
            for x in range(size):
                distance = ((x - 7.5) ** 2 + (y - 7.5) ** 2) ** 0.5
                if distance > radius:
                    continue
                edge = distance / radius
                value = gray_light if edge < 0.38 else gray_mid if edge < 0.72 else gray_dark
                alpha = round(170 - edge * 78 - frame * 18)
                set_pixel(pixels, x, y, with_alpha(value, max(58, alpha)))
    elif kind == "muzzle_flash":
        reach = 6 - frame
        for offset in range(-reach, reach + 1):
            alpha = max(45, 235 - abs(offset) * 28)
            set_pixel(pixels, 7 + offset, 7, with_alpha(orange if abs(offset) > 2 else white, alpha))
            set_pixel(pixels, 8, 7 + offset, with_alpha(gold if abs(offset) > 2 else white, alpha))
        for offset in range(-3 + frame, 4 - frame):
            set_pixel(pixels, 7 + offset, 7 + offset, with_alpha(gold, 180))
            set_pixel(pixels, 8 + offset, 7 - offset, with_alpha(orange, 160))
    elif kind == "impact_spark":
        reach = 4 - frame
        for offset in range(-reach, reach + 1):
            alpha = 255 - abs(offset) * 34
            set_pixel(pixels, 7 + offset, 7, with_alpha(white if abs(offset) <= 1 else gold, alpha))
            set_pixel(pixels, 7, 7 + offset, with_alpha(orange if abs(offset) > 1 else white, alpha))
        set_pixel(pixels, 8 + frame, 5 - frame, with_alpha(gold, 210))
        set_pixel(pixels, 5 - frame, 9 + frame, with_alpha(orange, 175))
    elif kind == "flame_core":
        heights = (7, 8, 7, 6)
        height = heights[frame]
        center = 7 + (frame % 2)
        for y in range(13, 13 - height, -1):
            progress = (13 - y) / max(1, height - 1)
            half_width = max(1, round((1.0 - progress) * 3.2))
            for x in range(center - half_width, center + half_width + 1):
                edge = abs(x - center) / max(1, half_width)
                value = white if edge < 0.35 and y > 8 else gold if edge < 0.75 else orange
                set_pixel(pixels, x, y, with_alpha(value, 245 if edge < 0.75 else 205))
        set_pixel(pixels, center + (-1 if frame % 2 else 1), 13 - height, with_alpha(gold, 190))
    elif kind == "flame_jet":
        heights = (10, 11, 10, 9)
        height = heights[frame]
        center = 7 + (frame & 1)
        for y in range(14, 14 - height, -1):
            progress = (14 - y) / max(1, height - 1)
            half_width = max(1, round(3.6 * (1.0 - progress) + 0.4))
            for x in range(center - half_width, center + half_width + 1):
                edge = abs(x - center) / max(1, half_width)
                if edge > 0.92:
                    value = ember
                elif edge > 0.55 or y < 6:
                    value = orange
                elif y > 10 and edge < 0.35:
                    value = white
                else:
                    value = gold
                set_pixel(pixels, x, y, value)
        tip_x = center + (-1 if frame in (1, 2) else 1)
        set_pixel(pixels, tip_x, 14 - height, with_alpha(orange, 255))
    elif kind == "energy_arc":
        paths = (
            [(3, 2), (6, 4), (5, 7), (9, 9), (8, 13)],
            [(11, 2), (8, 5), (10, 7), (6, 10), (5, 13)],
            [(5, 2), (8, 4), (7, 7), (11, 10), (9, 13)],
        )
        points = paths[frame]
        for index in range(len(points) - 1):
            x0, y0 = points[index]
            x1, y1 = points[index + 1]
            steps = max(abs(x1 - x0), abs(y1 - y0))
            for step in range(steps + 1):
                x = round(x0 + (x1 - x0) * step / steps)
                y = round(y0 + (y1 - y0) * step / steps)
                set_pixel(pixels, x, y, with_alpha(white, 255))
                set_pixel(pixels, x + 1, y, with_alpha(cyan, 185))
                set_pixel(pixels, x - 1, y, with_alpha(blue, 110))
    elif kind in ("blast_wave", "shockwave"):
        radius = 3 + frame * 2
        thickness = 1.15
        for y in range(size):
            for x in range(size):
                distance = ((x - 7.5) ** 2 + (y - 7.5) ** 2) ** 0.5
                if abs(distance - radius) <= thickness:
                    alpha = round(225 * (1.0 - abs(distance - radius) / thickness))
                    if kind == "blast_wave":
                        value = white if frame == 0 else gold if frame == 1 else orange
                    else:
                        value = white if frame == 0 else cyan if frame == 1 else violet
                    set_pixel(pixels, x, y, with_alpha(value, alpha))
    elif kind == "white_beam":
        bounds = ((6, 9), (7, 9), (7, 8))[frame]
        for y in range(bounds[0], bounds[1] + 1):
            for x in range(bounds[0], bounds[1] + 1):
                edge = x in bounds or y in bounds
                set_pixel(pixels, x, y, with_alpha(beam_edge if edge and frame < 2 else beam_white, 205 if edge and frame < 2 else 255))
    else:
        raise ValueError(f"unknown particle kind: {kind}")
    return pixels


def write_particles() -> None:
    PARTICLE_TEXTURE_DIR.mkdir(parents=True, exist_ok=True)
    PARTICLE_DEFINITION_DIR.mkdir(parents=True, exist_ok=True)
    for kind, frame_count in PARTICLE_FRAMES.items():
        textures = []
        for frame in range(frame_count):
            frame_name = f"{kind}_{frame}"
            write_png(PARTICLE_TEXTURE_DIR / f"{frame_name}.png", particle_texture(kind, frame))
            textures.append(f"guns:{frame_name}")
        definition = {"textures": textures}
        (PARTICLE_DEFINITION_DIR / f"{kind}.json").write_text(json.dumps(definition, indent=2) + "\n", encoding="utf-8")


def face(material: str) -> dict:
    return {"uv": PATCHES[material], "texture": "#body"}


def cuboid(name: str, start: list[float], end: list[float], material: str) -> dict:
    return {
        "name": name,
        "from": start,
        "to": end,
        "faces": {side: face(material) for side in ("north", "south", "east", "west", "up", "down")},
    }


WEAPON_DISPLAY = {
    "gui": {"rotation": [30, 225, 0], "translation": [0, 1.2, 0], "scale": [0.86, 0.86, 0.86]},
    "ground": {"translation": [0, 2.4, 0], "scale": [0.48, 0.48, 0.48]},
    "fixed": {"rotation": [0, 180, 0], "translation": [0, 0, -1.5], "scale": [0.72, 0.72, 0.72]},
    "thirdperson_righthand": {"rotation": [0, -90, 55], "translation": [1.13, 3.2, 1.13], "scale": [0.70, 0.70, 0.70]},
    "thirdperson_lefthand": {"rotation": [0, 90, -55], "translation": [1.13, 3.2, 1.13], "scale": [0.70, 0.70, 0.70]},
    "firstperson_righthand": {"rotation": [0, -90, 25], "translation": [1.0, 3.0, 1.0], "scale": [0.74, 0.74, 0.74]},
    "firstperson_lefthand": {"rotation": [0, 90, -25], "translation": [1.0, 3.0, 1.0], "scale": [0.74, 0.74, 0.74]},
}

PART_DISPLAY = {
    "gui": {"rotation": [30, 225, 0], "scale": [0.92, 0.92, 0.92]},
    "ground": {"translation": [0, 2.0, 0], "scale": [0.52, 0.52, 0.52]},
    "fixed": {"rotation": [0, 180, 0], "scale": [0.70, 0.70, 0.70]},
    "thirdperson_righthand": {"rotation": [0, -90, 55], "translation": [0, 2.8, 0], "scale": [0.64, 0.64, 0.64]},
    "thirdperson_lefthand": {"rotation": [0, 90, -55], "translation": [0, 2.8, 0], "scale": [0.64, 0.64, 0.64]},
    "firstperson_righthand": {"rotation": [0, -90, 25], "translation": [0.8, 2.4, 0.8], "scale": [0.68, 0.68, 0.68]},
    "firstperson_lefthand": {"rotation": [0, 90, -25], "translation": [0.8, 2.4, 0.8], "scale": [0.68, 0.68, 0.68]},
}

GEOMETRY = {
    "sniper_rifle": [
        ("barrel", [0.5, 7, 6], [7, 9, 9], "primary"),
        ("muzzle_outer", [0, 6, 5], [2, 10, 10], "metal"),
        ("muzzle_bore", [0, 7, 6.2], [0.55, 9, 8.8], "dark"),
        ("muzzle_collar", [1.8, 6.5, 5.5], [3.4, 9.5, 9.5], "dark"),
        ("barrel_highlight", [2.8, 8.5, 6.2], [7.5, 9.4, 8.8], "light"),
        ("receiver", [5, 5, 4], [12.5, 10.5, 11], "primary"),
        ("receiver_lower", [7, 4, 5], [12, 6, 10], "dark"),
        ("receiver_panel", [7, 6.2, 3.6], [10, 9.1, 4.3], "accent"),
        ("scope_front", [3.8, 11, 5.4], [6.2, 14.2, 9.6], "glow"),
        ("scope_glass", [3.5, 11.6, 6.1], [4, 13.6, 8.9], "light"),
        ("scope_tube", [5.5, 11.5, 5.8], [11, 14, 9.2], "primary"),
        ("scope_band", [7.1, 11.2, 5.5], [8.8, 14.3, 9.5], "metal"),
        ("scope_rear", [10.4, 11.1, 5.4], [12.8, 14.2, 9.6], "primary"),
        ("scope_mount", [8, 9.5, 6], [10, 12, 9], "dark"),
        ("stock", [12, 4, 5], [16, 10, 11], "accent"),
        ("stock_cutout", [14, 5.5, 6.8], [16, 8.5, 9.2], "shadow"),
        ("stock_butt", [15.2, 3.5, 4.5], [16, 10.5, 11.5], "accent"),
        ("grip", [10, 0, 6], [12.5, 5.2, 10], "secondary"),
        ("grip_shadow", [11.4, 0, 6.5], [13, 2.2, 9.5], "shadow"),
    ],
    "shotgun": [
        ("muzzle_outer", [0, 5.5, 3.5], [3.5, 11.7, 11.5], "accent"),
        ("muzzle_bore", [0, 7, 5], [0.65, 10, 10], "dark"),
        ("muzzle_lip", [0.6, 6.2, 4.2], [1.45, 11, 10.8], "light"),
        ("barrel", [2.8, 7.2, 5], [9, 10.2, 10], "primary"),
        ("receiver", [5, 5, 4], [13.8, 11.2, 11], "primary"),
        ("receiver_top", [6, 10.5, 4.5], [13, 12, 10.5], "dark"),
        ("receiver_highlight", [6, 8.8, 3.6], [11, 10.4, 4.3], "light"),
        ("top_sight", [6.5, 12, 6], [10.5, 14.5, 10], "light"),
        ("top_sight_shadow", [7.3, 12, 6.8], [9.7, 13, 9.2], "accent"),
        ("rear_cap", [12.8, 6, 4.5], [16, 11.5, 10.5], "primary"),
        ("rear_band", [14.2, 6.5, 4], [15.3, 11.8, 11], "dark"),
        ("grip", [9, 0, 6], [12, 6, 10.5], "accent"),
        ("grip_front", [8.4, 2, 5.5], [10, 6, 10], "light"),
        ("grip_shadow", [10.8, 0, 6.5], [12.8, 2.4, 10], "dark"),
    ],
    "grenade_launcher": [
        ("muzzle_outer", [0, 5, 2], [4, 13, 14], "accent"),
        ("muzzle_bore", [0, 6.8, 4], [0.7, 11.2, 12], "shadow"),
        ("muzzle_inner", [0.6, 7.5, 5], [1.4, 10.5, 11], "dark"),
        ("muzzle_highlight", [1, 11.8, 3], [3.4, 13.5, 13], "glow"),
        ("tube", [3, 6, 3], [14, 12, 13], "primary"),
        ("tube_top", [4.5, 11, 4], [13.5, 13, 12], "secondary"),
        ("tube_panel", [5, 7, 2.6], [11.5, 10, 3.5], "light"),
        ("rear_cap", [13, 6, 4], [16, 12, 12], "accent"),
        ("rear_band", [13.2, 5.5, 3.5], [14.4, 12.5, 12.5], "glow"),
        ("top_sight", [8, 12.5, 6], [11, 15, 10], "light"),
        ("top_sight_base", [7.3, 11.8, 5.5], [11.7, 13.2, 10.5], "dark"),
        ("grip", [9, 0, 6], [12.2, 6.3, 10.5], "primary"),
        ("grip_guard", [8.2, 2, 5.5], [10, 6.5, 11], "accent"),
        ("grip_shadow", [10.8, 0, 6.5], [12.8, 2.3, 10], "shadow"),
    ],
    "smg": [
        ("muzzle_outer", [0, 6.5, 4.5], [3, 11, 10.5], "light"),
        ("muzzle_bore", [0, 8, 6], [0.65, 9.7, 9], "shadow"),
        ("barrel", [2.4, 7.7, 5.7], [7, 9.8, 9.3], "primary"),
        ("receiver", [4, 5, 4], [13.5, 11, 11], "primary"),
        ("receiver_top", [5.2, 10, 4.5], [13, 12, 10.5], "dark"),
        ("side_emblem", [6.5, 7, 3.5], [10, 9.2, 4.3], "light"),
        ("top_sight", [7.5, 12, 6], [10.5, 14, 10], "light"),
        ("rear_frame", [12.5, 4.5, 4], [16, 11.5, 11], "light"),
        ("rear_cutout", [13.5, 6, 5.5], [16, 10, 9.5], "shadow"),
        ("rear_joint", [12, 6, 4.5], [14, 10.5, 10.5], "primary"),
        ("magazine", [6.5, 0.5, 6], [9.4, 5.5, 10], "accent"),
        ("grip", [10.5, 0, 6], [13, 5.5, 10], "accent"),
        ("grip_shadow", [11.8, 0, 6.4], [13.6, 2, 9.6], "dark"),
    ],
    "flamethrower": [
        ("nozzle_outer", [0, 5.5, 3.5], [3.6, 12, 11.5], "metal"),
        ("nozzle_bore", [0, 7, 5], [0.65, 10.5, 10], "shadow"),
        ("nozzle_inner", [1, 7.5, 5.5], [4.5, 10, 9.5], "primary"),
        ("front_brace", [3, 5, 3.5], [5.2, 12.5, 11.5], "accent"),
        ("housing", [4.3, 5, 4], [13.5, 11.5, 11], "primary"),
        ("housing_top", [6, 11, 5], [12.5, 13, 10], "dark"),
        ("top_plate", [7.5, 12.5, 6], [10.5, 14.5, 10], "metal"),
        ("side_panel", [6.5, 7, 3.5], [10.5, 9.8, 4.3], "accent"),
        ("rear_cap", [12.5, 5.5, 4.5], [16, 11, 10.5], "shadow"),
        ("tank", [8, 0, 7], [12.5, 6, 12.5], "secondary"),
        ("tank_highlight", [8.8, 1, 11.8], [11.6, 5, 13], "glow"),
        ("tank_neck", [9, 5.5, 8], [11.8, 7, 11.5], "metal"),
        ("hose", [6, 3, 4.2], [9, 5.2, 11.5], "accent"),
        ("grip", [12, 0, 5.5], [14.5, 5.5, 10], "shadow"),
        ("grip_cap", [11.3, 4.5, 5], [15, 6.2, 10.5], "accent"),
    ],
    "railgun": [
        ("front_plate", [0, 4.5, 2.5], [3.2, 13, 13.5], "primary"),
        ("front_bore", [0, 6.2, 4.5], [0.65, 11.2, 11.5], "shadow"),
        ("front_inner", [0.6, 7, 5.5], [2.2, 10.5, 10.5], "accent"),
        ("upper_rail", [2.5, 10, 4.5], [12.5, 12, 7], "glow"),
        ("lower_rail", [2.5, 6, 9], [12.5, 8, 11.5], "glow"),
        ("coil_upper_1", [3.5, 9, 4], [5.2, 13, 8], "accent"),
        ("coil_upper_2", [6, 9, 4], [7.7, 13, 8], "accent"),
        ("coil_upper_3", [8.5, 9, 4], [10.2, 13, 8], "accent"),
        ("coil_lower_1", [3.5, 5, 8], [5.2, 9, 12], "accent"),
        ("coil_lower_2", [6, 5, 8], [7.7, 9, 12], "accent"),
        ("coil_lower_3", [8.5, 5, 8], [10.2, 9, 12], "accent"),
        ("core", [3, 8, 6.5], [11.5, 10, 9.5], "glow"),
        ("rear_body", [10.5, 5, 3.5], [15, 12.5, 12.5], "primary"),
        ("rear_panel", [11.5, 7, 3], [14.5, 11, 4.2], "accent"),
        ("rear_panel_core", [12.2, 8, 2.7], [13.8, 10, 3.4], "metal"),
        ("rear_cap", [14, 5.5, 4], [16, 12, 12], "metal"),
        ("top_switch", [11.3, 12.5, 6], [14, 15, 10], "light"),
        ("grip", [11, 0, 6], [13.5, 6, 10], "primary"),
        ("grip_shadow", [12, 0, 6.5], [14, 2, 9.5], "shadow"),
    ],
    "rifle_round": [
        ("brass_base", [5, 1, 5], [11, 4, 11], "accent"),
        ("base_rim", [4.5, 0.5, 4.5], [11.5, 2, 11.5], "dark"),
        ("brass_neck", [5.5, 3, 5.5], [10.5, 6, 10.5], "accent"),
        ("blue_body", [5.5, 5, 5.5], [10.5, 12, 10.5], "primary"),
        ("blue_highlight", [5, 7, 6], [6, 11, 10], "light"),
        ("blue_shoulder", [6, 11, 6], [10, 14, 10], "primary"),
        ("tip", [6.8, 13, 6.8], [9.2, 15.5, 9.2], "glow"),
    ],
    "shotgun_shell": [
        ("brass_base", [4.5, 0.5, 4.5], [11.5, 4, 11.5], "accent"),
        ("base_rim", [4, 0, 4], [12, 1.5, 12], "dark"),
        ("red_body", [5, 3, 5], [11, 12, 11], "primary"),
        ("body_shadow", [9.8, 4, 5.2], [11.3, 11.5, 10.8], "secondary"),
        ("body_highlight", [4.7, 5, 6], [5.8, 10.5, 10], "light"),
        ("rolled_cap", [5.5, 11, 5.5], [10.5, 14, 10.5], "dark"),
        ("cap_center", [6.2, 12.5, 6.2], [9.8, 14.8, 9.8], "primary"),
    ],
    "grenade_round": [
        ("body", [3.5, 3, 3.5], [12.5, 12, 12.5], "primary"),
        ("body_top", [4.5, 11, 4.5], [11.5, 14, 11.5], "light"),
        ("body_shadow", [10.5, 4, 4], [13, 11.5, 12], "secondary"),
        ("gold_band", [3, 6.5, 3], [13, 8.7, 13], "accent"),
        ("band_shadow", [3.2, 6.3, 3.2], [12.8, 7, 12.8], "dark"),
        ("base", [4.5, 1.5, 4.5], [11.5, 3.5, 11.5], "dark"),
    ],
    "fuel_cell": [
        ("cell", [4, 2.5, 4], [12, 13, 12], "primary"),
        ("front_glass", [3.6, 4, 5.3], [4.3, 11.5, 10.7], "light"),
        ("rear_shadow", [11.5, 4, 5], [12.4, 11.5, 11], "secondary"),
        ("energy_core", [5.5, 4, 5.5], [10.5, 12, 10.5], "glow"),
        ("top", [4.5, 12.5, 4.5], [11.5, 15, 11.5], "metal"),
        ("top_cap", [6, 14.5, 6], [10, 16, 10], "light"),
        ("base", [4.5, 0.5, 4.5], [11.5, 3.5, 11.5], "metal"),
    ],
    "railgun_cell": [
        ("cell", [4, 2.5, 4], [12, 13, 12], "primary"),
        ("front_glass", [3.6, 4, 5.3], [4.3, 11.5, 10.7], "light"),
        ("rear_shadow", [11.5, 4, 5], [12.4, 11.5, 11], "secondary"),
        ("energy_core", [5.5, 4, 5.5], [10.5, 12, 10.5], "glow"),
        ("top", [4.5, 12.5, 4.5], [11.5, 15, 11.5], "metal"),
        ("top_cap", [6, 14.5, 6], [10, 16, 10], "light"),
        ("base", [4.5, 0.5, 4.5], [11.5, 3.5, 11.5], "metal"),
    ],
    "upgrade_template": [
        ("frame", [2, 3, 5], [14, 13, 11], "dark"),
        ("plate", [3, 4, 5.5], [13, 12, 10.5], "primary"),
        ("top_trim", [3, 11, 5], [13, 13.5, 11], "accent"),
        ("bottom_trim", [3, 2.5, 5], [13, 5, 11], "secondary"),
        ("socket", [5.5, 5.5, 4.7], [10.5, 10.5, 6.3], "glow"),
        ("socket_center", [7, 7, 4.4], [9, 9, 5.2], "light"),
    ],
    "precision_barrel": [
        ("barrel", [1, 7, 6], [14, 10, 10], "primary"),
        ("barrel_core", [2, 7.7, 5.5], [12, 9.3, 10.5], "glow"),
        ("muzzle", [0, 6, 5], [3, 11, 11], "metal"),
        ("muzzle_bore", [0, 7, 6], [0.6, 10, 10], "shadow"),
        ("ring", [7.5, 6, 5], [10, 11, 11], "accent"),
        ("mount", [11, 4, 5], [15, 9, 11], "secondary"),
        ("mount_lock", [12, 3.5, 6.5], [14, 5.5, 9.5], "light"),
    ],
    "cooling_system": [
        ("housing", [3.5, 3.5, 4], [12.5, 12.5, 12], "primary"),
        ("frame", [3, 3, 3.5], [13, 13, 5], "dark"),
        ("fan", [5, 5, 3], [11, 11, 5.5], "glow"),
        ("fan_hub", [7, 7, 2.7], [9, 9, 4], "light"),
        ("top", [5, 12, 5], [11, 14.5, 11], "light"),
        ("vent_left", [3, 5, 6], [4.2, 11, 10], "secondary"),
        ("vent_right", [11.8, 5, 6], [13, 11, 10], "secondary"),
        ("base", [5, 1.5, 5], [11, 4, 11], "metal"),
    ],
    "reinforced_receiver": [
        ("receiver", [3, 5, 4], [13, 11, 12], "primary"),
        ("reinforcement_top", [4, 9, 3], [12, 12, 13], "metal"),
        ("reinforcement_front", [2.5, 5, 3.5], [5, 11.5, 12.5], "metal"),
        ("chamber", [5, 6, 3], [10, 10, 5], "accent"),
        ("chamber_core", [6.5, 7, 2.7], [8.5, 9, 3.7], "glow"),
        ("side_plate", [7, 5.5, 11.5], [12, 10.5, 13], "dark"),
        ("mount", [10, 3, 5], [14, 8, 11], "secondary"),
        ("mount_lock", [11, 2.5, 6.5], [13.5, 4.5, 9.5], "light"),
    ],
}


def write_model(name: str) -> None:
    model = {
        "credit": "Guns owned reference-matched pixel-cuboid visual rebuild",
        "ambientocclusion": True,
        "gui_light": "front",
        "textures": {"body": f"guns:item/{name}", "particle": f"guns:item/{name}"},
        "display": WEAPON_DISPLAY if name in WEAPONS else PART_DISPLAY,
        "elements": [cuboid(*element) for element in GEOMETRY[name]],
    }
    (MODEL_DIR / f"{name}.json").write_text(json.dumps(model, indent=2) + "\n", encoding="utf-8")


def main() -> None:
    TEXTURE_DIR.mkdir(parents=True, exist_ok=True)
    MODEL_DIR.mkdir(parents=True, exist_ok=True)
    names = sorted(PALETTES)
    assert set(names) == set(GLYPHS) == set(GEOMETRY)
    for name in names:
        texture(name)
        write_model(name)
    write_particles()
    particle_frame_count = sum(PARTICLE_FRAMES.values())
    print(
        f"Generated {len(names)} item textures, {len(names)} models, "
        f"{particle_frame_count} particle textures, and {len(PARTICLE_FRAMES)} particle definitions."
    )


if __name__ == "__main__":
    main()
