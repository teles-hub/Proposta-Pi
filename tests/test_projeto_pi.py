"""Regression test for the numeric example published in README.md.

The application is a console program with no test seam, so the test drives the
real compiled program through stdin in a temporary directory. No file is
written inside the repository.
"""

import subprocess
import tempfile
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "ProjetoPi.java"

# Cartao VIP, um par de calcados, cadeia de descontos e encerramento.
STDIN_LINES = ["s", "1", "2", "200", "1", "40", "n", "n", "1", "s"]

EXPECTED_OUTPUT = (
    "Subtotal:",
    "1399,99",
    "Impostos (4,00%):",
    "56,00",
    "Taxas (6,00%):",
    "84,00",
    "Desconto VIP (5,00%):",
    "70,00",
    "TOTAL A PAGAR:",
    "1469,99",
    "TOTAL COM DESCONTO PIX: R$ 1396,49",
    "Faturamento Bruto do Dia:   R$ 1396,49",
)


class CalculoVendaTest(unittest.TestCase):
    def test_exemplo_do_readme_e_persistencia(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            tmp_path = Path(tmp)
            classes = tmp_path / "classes"
            principal = tmp_path / "principal"
            backup = tmp_path / "backup"
            classes.mkdir()

            compile_proc = subprocess.run(
                ["javac", "-encoding", "UTF-8", "-d", str(classes), str(SOURCE)],
                cwd=ROOT,
                capture_output=True,
                text=True,
                encoding="utf-8",
                errors="replace",
                timeout=120,
            )
            self.assertEqual(
                compile_proc.returncode,
                0,
                f"javac falhou:\n{compile_proc.stdout}\n{compile_proc.stderr}",
            )

            run_proc = subprocess.run(
                [
                    "java",
                    f"-Dnikestore.principal={principal}",
                    f"-Dnikestore.backup={backup}",
                    "-cp",
                    str(classes),
                    "ProjetoPi",
                ],
                cwd=ROOT,
                input="\n".join(STDIN_LINES) + "\n",
                capture_output=True,
                text=True,
                encoding="utf-8",
                errors="replace",
                timeout=120,
            )
            self.assertEqual(
                run_proc.returncode,
                0,
                f"java falhou:\n{run_proc.stdout}\n{run_proc.stderr}",
            )

            for expected in EXPECTED_OUTPUT:
                self.assertIn(expected, run_proc.stdout)

            for folder in (principal, backup):
                self.assertTrue(
                    (folder / "catalogo.txt").is_file(),
                    f"catalogo.txt não foi criado em {folder}",
                )
                self.assertTrue(
                    (folder / "relatorios.txt").is_file(),
                    f"relatorios.txt não foi criado em {folder}",
                )


if __name__ == "__main__":
    unittest.main()
