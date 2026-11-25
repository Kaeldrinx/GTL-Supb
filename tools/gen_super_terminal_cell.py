#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
「超级终端元件」像素材质生成器 —— AE 元件样式

设计语言（对齐 AE2 存储元件）
---------------------------
- 银色金属机身：左/上受光（1 像素硬边高光带），右/下压暗
- 顶部深色"接口带"：2 像素凹槽 + 2 颗浅色数据触点
- 中部"指示带"：元件类别色（本元件为物品+流体一体化 → 左半亮带 / 右半暗带）
- 底部连接触点：3 颗金属 pin，按帧交替亮灭
- 动画：指示带内的"储量条"逐帧 1→4→1 循环（8 帧无缝），触点每 4 帧翻转

规格
----
- 单帧 16x16，共 8 帧，垂直堆叠 → 16x128，透明背景
- 调色板严格 8 色（不含透明）；硬边像素、无抗锯齿、无渐变、无 3D、无文字
- 输出：
    super_terminal_cell.png
    super_terminal_cell.png.mcmeta   →  {"animation":{"frametime":2}}

运行
----
    pip install pillow
    python tools/gen_super_terminal_cell.py
可选：
    --out-dir <目录>   默认 src/main/resources/assets/gtlsupb/textures/item
    --preview <文件>   额外导出 8 帧横向预览
"""

from __future__ import annotations

import argparse
from pathlib import Path

try:
    from PIL import Image
except ImportError as exc:  # pragma: no cover
    raise SystemExit("需要 Pillow：pip install pillow") from exc

SIZE = 16
FRAMES = 8
NAME = "super_terminal_cell"
MCMETA = '{"animation":{"frametime":2}}\n'

T = (0, 0, 0, 0)

# ---- 恰好 8 色（不含透明）----
OUTLINE = (20, 22, 28, 255)      # 1 描边
BODY_L = (196, 202, 210, 255)    # 2 机身受光（左上）
BODY_M = (150, 157, 168, 255)    # 3 机身中调
BODY_D = (104, 110, 122, 255)    # 4 机身暗调（右下）
SLOT = (62, 67, 78, 255)         # 5 凹槽（顶部接口带 / 底部）
AE_BLUE = (58, 120, 190, 255)    # 6 AE 蓝（指示带主色）
AE_BLUE_L = (122, 186, 240, 255)  # 7 AE 亮蓝（储量条）
WHITE = (238, 242, 248, 255)     # 8 数据触点/标签


def blank():
    return [[T for _ in range(SIZE)] for _ in range(SIZE)]


def rect(px, x0, y0, x1, y1, c):
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            if 0 <= x < SIZE and 0 <= y < SIZE:
                px[y][x] = c


def outline_rect(px, x0, y0, x1, y1, c):
    for x in range(x0, x1 + 1):
        px[y0][x] = c
        px[y1][x] = c
    for y in range(y0, y1 + 1):
        px[y][x0] = c
        px[y][x1] = c


def draw_frame(i: int):
    px = blank()

    # ---------- 机身（x 2..13, y 1..14，AE 元件为竖长矩形）----------
    rect(px, 2, 1, 13, 14, BODY_M)
    outline_rect(px, 2, 1, 13, 14, OUTLINE)
    # 左上受光（1 像素硬边，无渐变）
    for x in range(3, 13):
        px[2][x] = BODY_L
    for y in range(2, 14):
        px[y][3] = BODY_L
    # 右下压暗（1 像素硬边）
    for x in range(3, 13):
        px[13][x] = BODY_D
    for y in range(2, 14):
        px[y][12] = BODY_D

    # ---------- 顶部深色接口带（y 3..4）+ 数据触点 ----------
    rect(px, 3, 3, 12, 4, SLOT)
    px[3][4] = WHITE
    px[3][9] = WHITE
    px[4][6] = BODY_L
    px[4][11] = BODY_L

    # ---------- 中部指示带（y 6..9）：左半主色 / 右半亮色 ----------
    rect(px, 3, 6, 12, 9, AE_BLUE)
    rect(px, 8, 6, 12, 9, AE_BLUE_L)
    px[6][3] = OUTLINE
    px[9][12] = OUTLINE

    # 储量条：逐帧 1→4→1（8 帧无缝），画在指示带中央的 4 个单元格内
    bars = [1, 2, 3, 4, 4, 3, 2, 1][i]
    cells = (5, 6, 8, 10)
    for k in range(4):
        x = cells[k]
        if k < bars:
            rect(px, x, 7, x, 8, WHITE)
        else:
            rect(px, x, 7, x, 8, AE_BLUE)

    # ---------- 底部（y 11..13）连接触点：每 4 帧翻转亮灭 ----------
    pins = (5, 8, 11)
    lit = i < 4
    for p in pins:
        px[12][p] = WHITE if lit else BODY_D
        px[13][p] = SLOT
    px[11][4] = BODY_D
    px[11][12] = BODY_D

    return px


def build_sheet():
    sheet = Image.new("RGBA", (SIZE, SIZE * FRAMES), T)
    for i in range(FRAMES):
        px = draw_frame(i)
        for y in range(SIZE):
            for x in range(SIZE):
                sheet.putpixel((x, i * SIZE + y), px[y][x])
    return sheet


def default_out_dir() -> Path:
    here = Path(__file__).resolve()
    for parent in here.parents:
        cand = parent / "src" / "main" / "resources" / "assets" / "gtlsupb" / "textures" / "item"
        if cand.parent.parent.parent.exists():
            return cand
    return Path.cwd()


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--out-dir", default=None)
    ap.add_argument("--preview", default=None)
    args = ap.parse_args()

    out = Path(args.out_dir) if args.out_dir else default_out_dir()
    out.mkdir(parents=True, exist_ok=True)

    sheet = build_sheet()
    assert sheet.size == (SIZE, SIZE * FRAMES), sheet.size
    png = out / f"{NAME}.png"
    sheet.save(png, format="PNG")
    (out / f"{NAME}.png.mcmeta").write_text(MCMETA, encoding="utf-8")

    if args.preview:
        preview = Image.new("RGBA", (SIZE * FRAMES, SIZE), T)
        for i in range(FRAMES):
            preview.paste(sheet.crop((0, i * SIZE, SIZE, (i + 1) * SIZE)), (i * SIZE, 0))
        preview.save(args.preview)

    used = {c for c in sheet.getdata() if c[3] != 0}
    print(f"OK {png}  size={sheet.size[0]}x{sheet.size[1]}  opaque-colors={len(used)}")
    print(f"OK {png}.mcmeta  {MCMETA.strip()}")


if __name__ == "__main__":
    main()
