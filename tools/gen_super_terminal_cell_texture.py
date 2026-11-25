#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
生成「超级终端元件」的精确像素材质（16x128 动画贴图 + mcmeta）。

规格
----
- 单帧 16x16 像素，共 8 帧纵向排列 → 成品 16x128
- 纯手写像素、无插值：所有绘制都落在整数像素上（不使用 resize/scale/滤镜）
- 无模糊、无抗锯齿、无 3D 光影、无 AI 风格：仅使用有限调色板 + 硬边
- 输出：
    assets/gtlsupb/textures/item/super_terminal_cell.png       (16x128)
    assets/gtlsupb/textures/item/super_terminal_cell.png.mcmeta (动画定义)

运行
----
    pip install pillow
    python tools/gen_super_terminal_cell_texture.py

可选参数：
    --out-dir <目录>     指定输出根目录（默认自动定位到 src/main/resources）
    --preview <文件>     额外输出一张 8 帧横向预览图（默认不输出）
"""

from __future__ import annotations

import argparse
import os
from pathlib import Path

try:
    from PIL import Image
except ImportError as exc:  # pragma: no cover
    raise SystemExit("需要 Pillow：pip install pillow") from exc

FRAME = 16
FRAMES = 8
W, H = FRAME, FRAME * FRAMES

# ---------------------------------------------------------------- 调色板
TRANSPARENT = (0, 0, 0, 0)
OUTLINE = (18, 20, 26, 255)        # 外壳描边
CASING = (44, 49, 60, 255)         # 外壳主色
CASING_HI = (66, 73, 88, 255)      # 外壳高光（硬边，单像素）
CASING_LO = (30, 34, 43, 255)      # 外壳阴影
GLASS = (24, 40, 58, 255)          # 内屏底
GLASS_HI = (40, 66, 92, 255)
ITEM_SLOT = (196, 96, 128, 255)    # 物品区标记（品红）
ITEM_SLOT_HI = (232, 140, 168, 255)
FLUID_SLOT = (72, 168, 200, 255)   # 流体区标记（青蓝）
FLUID_SLOT_HI = (124, 208, 232, 255)
CORE = (120, 232, 168, 255)        # 核心（脉冲，逐帧变化）
CORE_DIM = (56, 132, 96, 255)
BIT = (232, 220, 120, 255)         # 数据位
PLATE = (126, 132, 148, 255)       # 上下端接口
PLATE_HI = (168, 174, 188, 255)


def blank() -> list[list[tuple[int, int, int, int]]]:
    return [[TRANSPARENT for _ in range(W)] for _ in range(H and FRAME)]


def put(px, x: int, y: int, color) -> None:
    if 0 <= x < FRAME and 0 <= y < FRAME:
        px[y][x] = color


def rect(px, x0: int, y0: int, x1: int, y1: int, color) -> None:
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            put(px, x, y, color)


def outline_rect(px, x0: int, y0: int, x1: int, y1: int, color) -> None:
    for x in range(x0, x1 + 1):
        put(px, x, y0, color)
        put(px, x, y1, color)
    for y in range(y0, y1 + 1):
        put(px, x0, y, color)
        put(px, x1, y, color)


def draw_frame(index: int) -> list[list[tuple[int, int, int, int]]]:
    """绘制第 index 帧（0..7）。脉冲周期 8 帧：0..3 亮起、4..7 回落。"""
    px = blank()

    # 上/下端接口（像"元件金手指"）
    rect(px, 4, 0, 11, 0, PLATE)
    rect(px, 4, 1, 11, 1, PLATE_HI)
    rect(px, 4, 15, 11, 15, PLATE)
    rect(px, 4, 14, 11, 14, PLATE_HI)

    # 外壳
    rect(px, 2, 2, 13, 13, CASING)
    outline_rect(px, 2, 2, 13, 13, OUTLINE)
    # 硬边高光/阴影（各 1 像素，不做渐变）
    for x in range(3, 13):
        put(px, x, 3, CASING_HI)
    for x in range(3, 13):
        put(px, x, 12, CASING_LO)
    put(px, 3, 4, CASING_HI)
    put(px, 3, 11, CASING_LO)

    # 内屏
    rect(px, 4, 4, 11, 11, GLASS)
    outline_rect(px, 4, 4, 11, 11, GLASS_HI)

    # 双仓标记：左上 3x3 = 物品（品红），右下 3x3 = 流体（青蓝）
    rect(px, 5, 5, 7, 7, ITEM_SLOT)
    put(px, 5, 5, ITEM_SLOT_HI)
    rect(px, 8, 8, 10, 10, FLUID_SLOT)
    put(px, 8, 8, FLUID_SLOT_HI)

    # 中央核心：按帧脉冲（亮 4 帧 / 暗 4 帧），保持硬边
    bright = index < 4
    core_color = CORE if bright else CORE_DIM
    put(px, 6, 6, core_color)
    put(px, 6, 9, core_color)
    put(px, 9, 6, core_color)
    put(px, 9, 9, core_color)

    # 数据位：绕核心逐帧旋转（体现"搬运进元件"）
    bits = [(6, 7), (7, 6), (9, 7), (8, 6), (9, 8), (8, 9), (6, 8), (7, 9)]
    bx, by = bits[index % len(bits)]
    put(px, bx, by, BIT)
    bx2, by2 = bits[(index + 4) % len(bits)]
    put(px, bx2, by2, BIT)

    return px


def build_sheet() -> Image.Image:
    sheet = Image.new("RGBA", (W, H), TRANSPARENT)
    for i in range(FRAMES):
        px = draw_frame(i)
        for y in range(FRAME):
            for x in range(FRAME):
                sheet.putpixel((x, i * FRAME + y), px[y][x])
    return sheet


MCMETA = """{
  "animation": {
    "frametime": 2,
    "interpolate": false
  }
}
"""


def default_out_dir() -> Path:
    here = Path(__file__).resolve()
    for parent in here.parents:
        candidate = parent / "src" / "main" / "resources" / "assets" / "gtlsupb" / "textures" / "item"
        if candidate.parent.parent.parent.exists():
            return candidate
    return Path.cwd() / "textures"


def main() -> None:
    ap = argparse.ArgumentParser()
    ap.add_argument("--out-dir", default=None)
    ap.add_argument("--preview", default=None)
    args = ap.parse_args()

    out_dir = Path(args.out_dir) if args.out_dir else default_out_dir()
    out_dir.mkdir(parents=True, exist_ok=True)

    sheet = build_sheet()
    png = out_dir / "super_terminal_cell.png"
    sheet.save(png, format="PNG", optimize=False)
    (out_dir / "super_terminal_cell.png.mcmeta").write_text(MCMETA, encoding="utf-8")

    if args.preview:
        preview = Image.new("RGBA", (W * FRAMES, FRAME), TRANSPARENT)
        for i in range(FRAMES):
            preview.paste(sheet.crop((0, i * FRAME, W, (i + 1) * FRAME)), (i * W, 0))
        preview.save(args.preview)

    size = os.path.getsize(png)
    print(f"OK  {png}  ({W}x{H}, {size} bytes)")
    print(f"OK  {png}.mcmeta  (frametime=2, interpolate=false)")


if __name__ == "__main__":
    main()
