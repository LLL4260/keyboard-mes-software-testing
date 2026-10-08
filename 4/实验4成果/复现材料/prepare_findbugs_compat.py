"""在仓库外生成 FindBugs 3.0.1 的实验兼容副本；不修改原始 JAR。

用法：python prepare_findbugs_compat.py 原版findbugs.jar JDK17目录 临时输出目录
运行时需将 ASM 9.7 的 asm、asm-tree、asm-commons 放在类路径最前。
本脚本不提供官方兼容性保证，也不将检测结果解释为没有缺陷。
"""
import struct
import sys
from pathlib import Path
from zipfile import ZipFile, ZIP_DEFLATED


def adapt_class(data):
    data = bytearray(data)
    count = struct.unpack_from('>H', data, 8)[0]
    pos, index, changes = 10, 1, 0
    while index < count:
        tag = data[pos]
        pos += 1
        if tag == 1:
            pos += 2 + struct.unpack_from('>H', data, pos)[0]
        elif tag == 3:
            if struct.unpack_from('>I', data, pos)[0] == 327680:
                struct.pack_into('>I', data, pos, 589824)
                changes += 1
            pos += 4
        elif tag == 4:
            pos += 4
        elif tag in (5, 6):
            pos += 8
            index += 1
        elif tag in (7, 8, 16, 19, 20):
            pos += 2
        elif tag in (9, 10, 11, 12, 17, 18):
            pos += 4
        elif tag == 15:
            pos += 3
        else:
            raise ValueError(f'Unknown constant pool tag {tag}')
        index += 1
    return data, changes


def main():
    original, jdk, output = map(Path, sys.argv[1:])
    output.mkdir(parents=True, exist_ok=True)
    adapted = output / 'findbugs-api9.jar'
    if original.resolve() == adapted.resolve():
        raise ValueError('输出不能覆盖原始工具')
    changes = 0
    with ZipFile(original) as source, ZipFile(adapted, 'w', ZIP_DEFLATED) as target:
        for info in source.infolist():
            data = source.read(info.filename)
            if info.filename.endswith('.class'):
                data, amount = adapt_class(data)
                changes += amount
            if info.filename == 'META-INF/MANIFEST.MF':
                # 删除旧的相对依赖类路径，避免意外加载原配 ASM5。
                data = b'Manifest-Version: 1.0\r\n\r\n'
            target.writestr(info, data)
    if changes != 13:
        raise ValueError(f'输入版本与已核验版本不一致，实际修改 {changes} 个常量')
    for module in ('java.base', 'java.rmi'):
        with ZipFile(jdk / 'jmods' / f'{module}.jmod') as source, ZipFile(
                output / f'java17-{module.split(".")[1]}.jar', 'w', ZIP_DEFLATED) as target:
            for name in source.namelist():
                if name.startswith('classes/') and name.endswith('.class') and not name.endswith('module-info.class'):
                    target.writestr(name[8:], source.read(name))
    print(f'已生成兼容副本；ASM API 参数修改 {changes} 处')


if __name__ == '__main__':
    main()
