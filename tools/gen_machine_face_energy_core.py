#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
GTM 机器面动画覆盖层材质生成器（精确像素，无抗锯齿/无渐变/无 3D/AI 风格）

规格
----
- 每帧 16x16，共 8 帧，垂直堆叠 → 总尺寸 16x128，透明背景
- 动画无缝循环：能量核心脉冲（1-4 帧渐亮，5-8 帧渐暗）+ 8 颗铆钉每帧旋转一格（8 步 = 一整圈）
- 调色板严格 8 色（不含透明），GTM 工业风：暗钢灰 + 铜色/橙色能量点缀，左上光源、硬边缘
- 输出：
    machine_face_energy_core.png
    machine_face_energy_core.png.mcmeta   →  {"animation":{"frametime":2}}

运行
----
    pip install pillow
    python tools/gen_machine_face_energy_core.py
可选：
    --out-dir <目录>   输出目录（默认 src/main/resources/assets/gtlsupb/textures/block）
    --preview <文件>   额外导出 8 帧横向预览
"""

from __future__ import annotations

import argparse
import math
from pathlib import Path

try:
    from PIL import Image
except ImportError as exc:  # pragma: no cover
    raise SystemExit("需要 Pillow：pip install pillow") from exc

SIZE = 16
FRAMES = 8
NAME = "machine_face_energy_core"
MCMETA = '{"animation":{"frametime":2}}\n'

T = (0, 0, 0, 0)

# ---- 调色板：恰好 8 色（索引 1..8），不含透明 ----
P = {
    "outline": (16, 18, 22, 255),      # 1 外壳描边（最暗）
    "steel_d": (43, 49, 56, 255),      # 2 暗钢灰（主体）
    "steel_m": (69, 77, 87, 255),      # 3 中灰（受光面）
    "steel_l": (111, 120, 133, 255),   # 4 高光灰（左上光源）
    "copper_d": (138, 90, 43, 255),    # 5 暗铜
    "copper": (201, 122, 43, 255),     # 6 铜色
    "orange": (240, 169, 74, 255),     # 7 橙色能量
    "glow": (247, 210, 122, 255),      # 8 高亮能量
}

CX = CY = 7.5  # 面板中心（16x16 时为 7.5，取整后落在像素中心）


def frame_canvas():
    return [[T for _ in range(SIZE)] for _ in range(SIZE)]


def put(px, x, y, color):
    if 0 <= x < SIZE and 0 <= y < SIZE:
        px[y][x] = color


def rounded_ring(inner: float, outer: float, color):
    """硬边圆环：逐像素判定半径（不做插值，天然无抗锯齿）。"""
    pts = []
    for y in range(SIZE):
        for x in range(SIZE):
            dx = x - CX
            dy = y - CY
            d = math.hypot(dx, dy)
            if inner <= d <= outer:
                pts.append((x, y))
    for (x, y) in pts:
        put(px_global, x, y, color)
    return pts


def draw_frame(index: int):
    global px_global
    px = frame_canvas()
    px_global = px

    # 1) 底板：矩形机身 + 左上受光的硬边高光带（无渐变）
    for y in range(1, 15):
        for x in range(1, 15):
            px[y][x] = P["steel_d"]
    for x in range(2, 14):                       # 顶面 1 像素受光
        px[2][x] = P["steel_m"]
    for y in range(2, 14):                       # 左面 1 像素受光（左上光源）
        px[y][2] = P["steel_m"]
    px[2][2] = P["steel_l"]                      # 左上角高光
    for x in range(2, 14):                       # 底/右 1 像素暗边
        px[13][x] = P["outline"]
    for y in range(2, 14):
        px[y][13] = P["outline"]

    # 2) 四角铆钉（铜色，固定不动）
    for (bx, by) in ((3, 3), (12, 3), (3, 12), (12, 12)):
        put(px, bx, by, P["copper_d"])

    # 3) 中心能量核心：外铜环（不动）+ 内能量环（8 步旋转的缺口）
    for y in range(SIZE):
        for x in range(SIZE):
            d = math.hypot(x - CX, y - CY)
            if 6.0 <= d <= 7.0:
                px[y][x] = P["copper_d"]
            elif 4.0 <= d < 6.0:
                px[y][x] = P["steel_m"]

    # 内环 8 颗能量齿，每帧旋转 1 格（8 帧 = 一整圈，完美循环）
    for k in range(8):
        ang = math.radians((k * 45) + index * 45)
        gx = int(round(CX + math.cos(ang) * 5.0))
        gy = int(round(CY + math.sin(ang) * 5.0))
        color = P["orange"] if k % 2 == 0 else P["copper"]
        put(px, gx, gy, color)

    # 4) 核心脉冲：1-4 帧渐亮、5-8 帧渐暗（颜色档位切换，非渐变）
    pulse = [0, 1, 2, 3, 3, 2, 1, 0][index]     # 0..3 档
    core_colors = (P["copper_d"], P["copper"], P["orange"], P["glow"])
    core = core_colors[pulse]
    for (x, y) in ((7, 7), (8, 7), (7, 8), (8, 8)):
        put(px, x, y, core)
    if pulse >= 2:                               # 亮档额外 4 像素光晕（硬边）
        for (x, y) in ((7, 6), (8, 6), (6, 7), (6, 8)):
            put(px, x, y, P["orange"])
    if pulse == 3:                               # 最亮档再加一圈，形成"过载"帧
        for (x, y) in ((9, 7), (7, 9), (9, 8), (8, 9)):
            put(px, x, y, P["glow"])

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
        cand = parent / "src" / "main" / "resources" / "assets" / "gtlsupb" / "textures" / "block"
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

    colors = {c for row in [sheet.getdata()] for c in row if c[3] != 0}
    print(f"OK {png} size={sheet.size} opaque-colors={len(colors)}")
    print(f"OK {png}.mcmeta -> {MCMETA.strip()}")


if __name__ == "__main__":
    main()
