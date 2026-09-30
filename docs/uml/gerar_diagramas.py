#!/usr/bin/env python3
"""
Gera os diagramas UML do projeto (PNG + SVG) a partir dos arquivos .puml desta pasta.

Uso (a partir de qualquer diretório):
    python3 docs/uml/gerar_diagramas.py            # renderiza PNG e SVG
    python3 docs/uml/gerar_diagramas.py --check    # apenas valida a sintaxe dos .puml

Requisitos: Java 17+ (o mesmo JDK do projeto). Não exige Graphviz: os diagramas de
classes usam o layout interno do PlantUML (smetana, via "!pragma layout smetana").
O plantuml.jar é baixado uma única vez para ~/.cache/plantuml/plantuml.jar
(ou informe outro caminho pela variável de ambiente PLANTUML_JAR).
"""
import os
import pathlib
import shutil
import subprocess
import sys
import urllib.request

PLANTUML_URL = "https://github.com/plantuml/plantuml/releases/latest/download/plantuml.jar"
PASTA_UML = pathlib.Path(__file__).resolve().parent


def localizar_jar() -> pathlib.Path:
    informado = os.environ.get("PLANTUML_JAR")
    if informado and pathlib.Path(informado).is_file():
        return pathlib.Path(informado)

    cache = pathlib.Path.home() / ".cache" / "plantuml" / "plantuml.jar"
    if cache.is_file():
        return cache

    cache.parent.mkdir(parents=True, exist_ok=True)
    print(f"Baixando PlantUML ({PLANTUML_URL})\n  -> {cache}")
    urllib.request.urlretrieve(PLANTUML_URL, cache)
    return cache


def main() -> None:
    if not shutil.which("java"):
        sys.exit("Java não encontrado no PATH. Instale um JDK 17+ e tente novamente.")

    fontes = sorted(PASTA_UML.glob("*.puml"))
    if not fontes:
        sys.exit(f"Nenhum arquivo .puml encontrado em {PASTA_UML}.")

    jar = localizar_jar()
    modos = ["-checkonly"] if "--check" in sys.argv else ["-tpng", "-tsvg"]

    for modo in modos:
        comando = [
            "java", "-Djava.awt.headless=true", "-DPLANTUML_LIMIT_SIZE=16384",
            "-jar", str(jar), "-failfast2", "-charset", "UTF-8", modo,
            *[str(f) for f in fontes],
        ]
        print("$", " ".join(comando[:8]), "...")
        subprocess.run(comando, check=True, cwd=PASTA_UML)

    if "--check" in sys.argv:
        print("Sintaxe OK em", len(fontes), "arquivo(s).")
        return

    for fonte in fontes:
        png = fonte.with_suffix(".png")
        svg = fonte.with_suffix(".svg")
        print(f"  OK  {png.relative_to(PASTA_UML.parent.parent)}  |  {svg.name}")


if __name__ == "__main__":
    main()
