#!/usr/bin/env python3
"""
Convert old CS.410 automaton exam files to the new (post-1.4.x) syntax.

Source-of-truth syntax: README files under src/main/java/{DFA,NFA,PDA,TM,CFG,Regex}/
and SymbolConstants.java. Old-format characteristics surveyed from
/Users/selim/Downloads/Arşiv (Fall-2025 student submissions).

Per-machine transformations:
  .dfa  Capitalized headers -> lowercase. `Finals:` -> `accept:`.
        `q0 -> q1 (a b c)` expands to one comma-form line per symbol.
  .nfa  Same as DFA; `eps` symbols supported inside parens.
  .pda  `alphabet:` -> `input:`, `stack_alphabet:` -> `stack:`,
        `finals:` -> `accept:`. Whitespace transitions get commas.
  .tm   `input_alphabet:` -> `input:`, `tape_alphabet:` -> `tape:`,
        `REJECT:` -> `reject:`. Whitespace transitions get commas.
  .cfg  `Variables =` -> `vars:`, `Terminals =` -> `terminals:`,
        `Start =` -> `start:`. Insert `rules:` header before first production.
  .rex  Two-line (pattern, alphabet) -> labelled `alphabet:` / `pattern:` form.
  .test Format unchanged; copied verbatim.

Already-new files are detected and copied verbatim (idempotent).
"""

from __future__ import annotations

import argparse
import csv
import glob
import os
import re
import shutil
import subprocess
import sys
from dataclasses import dataclass, field
from pathlib import Path

PROJECT_ROOT = Path(__file__).resolve().parent.parent
DEFAULT_JAR_GLOB = str(PROJECT_ROOT / "target" / "CS410-Exam-*.jar")

AUTOMATON_EXTS = {".dfa", ".nfa", ".pda", ".tm", ".cfg", ".rex"}
PASSTHROUGH_EXTS = {".test"}


# ---------------------------------------------------------------------------
# Generic helpers
# ---------------------------------------------------------------------------

def read_text(path: Path) -> str:
    raw = path.read_bytes()
    if raw.startswith(b"\xef\xbb\xbf"):
        raw = raw[3:]
    text = raw.decode("utf-8", errors="replace")
    return text.replace("\r\n", "\n").replace("\r", "\n")


def write_atomic(path: Path, text: str) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    tmp = path.with_suffix(path.suffix + ".tmp")
    tmp.write_text(text, encoding="utf-8")
    tmp.replace(path)


def is_blank(line: str) -> bool:
    return not line.strip()


def is_comment(line: str) -> bool:
    return line.lstrip().startswith("#")


HEADER_RE = re.compile(r"^\s*([A-Za-z_][A-Za-z0-9_]*)\s*[:=]\s*(.*?)\s*$")
BARE_KEYWORDS = {"transitions", "rules"}


def parse_header(line: str) -> tuple[str, str] | None:
    m = HEADER_RE.match(line)
    if m:
        return m.group(1).lower(), m.group(2)
    bare = line.strip().lower()
    if bare in BARE_KEYWORDS:
        return bare, ""
    return None


# ---------------------------------------------------------------------------
# Idempotency detection
# ---------------------------------------------------------------------------

def is_already_new(text: str, ext: str) -> bool:
    headers: set[str] = set()
    for line in text.splitlines():
        h = parse_header(line)
        if h is not None:
            headers.add(h[0])
    if ext == ".dfa" or ext == ".nfa":
        return "states" in headers and "accept" in headers and "alphabet" in headers
    if ext == ".pda":
        return "input" in headers and "stack" in headers and "accept" in headers
    if ext == ".tm":
        return "input" in headers and "tape" in headers
    if ext == ".cfg":
        return "vars" in headers or "rules" in headers
    if ext == ".rex":
        return "pattern" in headers or "alphabet" in headers
    return False


# ---------------------------------------------------------------------------
# DFA / NFA converter
# ---------------------------------------------------------------------------

# DFA/NFA old transition: `qX -> qY (a b c)` (allow no-space arrow)
TRANS_FA_RE = re.compile(r"^\s*(\S+)\s*->\s*(\S+)\s*\(([^)]*)\)\s*$")
HDR_FA_MAP = {
    "start": "start",
    "finals": "accept",
    "final": "accept",
    "accept": "accept",
    "alphabet": "alphabet",
    "states": "states",
    "transitions": "transitions",
}


def convert_fa(text: str, ext: str) -> str:
    fields: dict[str, str] = {}
    transitions: list[str] = []  # list of formatted lines or "" for blank separators
    in_transitions = False
    last_header_key: str | None = None  # for value-on-next-line continuations

    for raw in text.splitlines():
        if is_comment(raw):
            if in_transitions:
                transitions.append(raw.rstrip())
            continue
        if is_blank(raw):
            if in_transitions:
                transitions.append("")
            else:
                last_header_key = None
            continue

        hdr = parse_header(raw)
        if hdr is not None and hdr[0] in HDR_FA_MAP and hdr[0] != "transitions":
            new_key = HDR_FA_MAP[hdr[0]]
            fields[new_key] = hdr[1].strip()
            last_header_key = new_key
            in_transitions = False
            continue
        if hdr is not None and hdr[0] == "transitions":
            in_transitions = True
            last_header_key = None
            continue

        # Transition line.
        if "->" in raw:
            m = TRANS_FA_RE.match(raw)
            if m:
                src, dst, syms = m.group(1), m.group(2), m.group(3).split()
                for s in syms:
                    transitions.append(f"{src}, {s} -> {dst}")
            else:
                # Already-new comma form or unrecognized — keep verbatim.
                transitions.append(raw.strip())
            in_transitions = True
            last_header_key = None
            continue

        # Continuation: bare tokens following a header with empty value.
        if not in_transitions and last_header_key is not None:
            existing = fields.get(last_header_key, "")
            extra = raw.strip()
            fields[last_header_key] = (existing + " " + extra).strip() if existing else extra

    # Trim trailing blank entries
    while transitions and transitions[-1] == "":
        transitions.pop()

    out: list[str] = []
    for key in ("states", "alphabet", "start", "accept"):
        if key in fields:
            out.append(f"{key}: {fields[key]}")
    out.append("")
    out.append("transitions:")
    out.extend(transitions)
    out.append("")
    return "\n".join(out)


# ---------------------------------------------------------------------------
# PDA converter
# ---------------------------------------------------------------------------

HDR_PDA_MAP = {
    "states": "states",
    "alphabet": "input",
    "input": "input",
    "stack_alphabet": "stack",
    "stack": "stack",
    "start": "start",
    "stack_start": "stack_start",
    "finals": "accept",
    "accept": "accept",
    "transitions": "transitions",
}


def convert_pda(text: str) -> str:
    fields: dict[str, str] = {}
    transitions: list[str] = []
    in_transitions = False
    last_header_key: str | None = None

    for raw in text.splitlines():
        if is_comment(raw):
            if in_transitions:
                transitions.append(raw.rstrip())
            continue
        if is_blank(raw):
            if in_transitions:
                transitions.append("")
            else:
                last_header_key = None
            continue

        hdr = parse_header(raw)
        if hdr is not None and hdr[0] in HDR_PDA_MAP and hdr[0] not in ("transitions",):
            new_key = HDR_PDA_MAP[hdr[0]]
            fields[new_key] = hdr[1].strip()
            last_header_key = new_key
            in_transitions = False
            continue
        if hdr is not None and hdr[0] == "transitions":
            in_transitions = True
            last_header_key = None
            continue

        # Transition: `state inp pop -> newState push`
        if "->" in raw:
            lhs, rhs = raw.split("->", 1)
            lhs_tokens = lhs.replace(",", " ").split()
            rhs_tokens = rhs.replace(",", " ").split()
            if len(lhs_tokens) == 3 and len(rhs_tokens) == 2:
                transitions.append(
                    f"{lhs_tokens[0]}, {lhs_tokens[1]}, {lhs_tokens[2]} -> "
                    f"{rhs_tokens[0]}, {rhs_tokens[1]}"
                )
            else:
                # Pass through unrecognized; the validator will flag it.
                transitions.append(raw.strip())
            in_transitions = True
            last_header_key = None
            continue

        if not in_transitions and last_header_key is not None:
            existing = fields.get(last_header_key, "")
            extra = raw.strip()
            fields[last_header_key] = (existing + " " + extra).strip() if existing else extra

    while transitions and transitions[-1] == "":
        transitions.pop()

    out: list[str] = []
    for key in ("states", "input", "stack", "start", "stack_start", "accept"):
        if key in fields:
            out.append(f"{key}: {fields[key]}")
    out.append("")
    out.append("transitions:")
    out.extend(transitions)
    out.append("")
    return "\n".join(out)


# ---------------------------------------------------------------------------
# TM converter
# ---------------------------------------------------------------------------

HDR_TM_MAP = {
    "states": "states",
    "input_alphabet": "input",
    "input": "input",
    "tape_alphabet": "tape",
    "tape": "tape",
    "start": "start",
    "accept": "accept",
    "reject": "reject",
    "transitions": "transitions",
}


def convert_tm(text: str) -> str:
    fields: dict[str, str] = {}
    transitions: list[str] = []
    in_transitions = False
    last_header_key: str | None = None

    for raw in text.splitlines():
        if is_comment(raw):
            if in_transitions:
                transitions.append(raw.rstrip())
            continue
        if is_blank(raw):
            if in_transitions:
                transitions.append("")
            else:
                last_header_key = None
            continue

        hdr = parse_header(raw)
        if hdr is not None and hdr[0] in HDR_TM_MAP and hdr[0] != "transitions":
            new_key = HDR_TM_MAP[hdr[0]]
            fields[new_key] = hdr[1].strip()
            last_header_key = new_key
            in_transitions = False
            continue
        if hdr is not None and hdr[0] == "transitions":
            in_transitions = True
            last_header_key = None
            continue

        if "->" in raw:
            lhs, rhs = raw.split("->", 1)
            lhs_tokens = lhs.replace(",", " ").split()
            rhs_tokens = rhs.replace(",", " ").split()
            if len(lhs_tokens) == 2 and len(rhs_tokens) == 3:
                transitions.append(
                    f"{lhs_tokens[0]}, {lhs_tokens[1]} -> "
                    f"{rhs_tokens[0]}, {rhs_tokens[1]}, {rhs_tokens[2]}"
                )
            else:
                transitions.append(raw.strip())
            in_transitions = True
            last_header_key = None
            continue

        if not in_transitions and last_header_key is not None:
            existing = fields.get(last_header_key, "")
            extra = raw.strip()
            fields[last_header_key] = (existing + " " + extra).strip() if existing else extra

    while transitions and transitions[-1] == "":
        transitions.pop()

    out: list[str] = []
    for key in ("states", "input", "tape", "start", "accept", "reject"):
        if key in fields:
            out.append(f"{key}: {fields[key]}")
    out.append("")
    out.append("transitions:")
    out.extend(transitions)
    out.append("")
    return "\n".join(out)


# ---------------------------------------------------------------------------
# CFG converter
# ---------------------------------------------------------------------------

HDR_CFG_MAP = {
    "variables": "vars",
    "vars": "vars",
    "terminals": "terminals",
    "start": "start",
    "rules": "rules",
}


def convert_cfg(text: str) -> str:
    fields: dict[str, str] = {}
    rules: list[str] = []
    in_rules = False
    last_header_key: str | None = None

    for raw in text.splitlines():
        if is_comment(raw):
            if in_rules:
                rules.append(raw.rstrip())
            continue
        if is_blank(raw):
            if in_rules:
                rules.append("")
            else:
                last_header_key = None
            continue

        hdr = parse_header(raw)
        if hdr is not None and hdr[0] in HDR_CFG_MAP and hdr[0] != "rules":
            new_key = HDR_CFG_MAP[hdr[0]]
            fields[new_key] = hdr[1].strip()
            last_header_key = new_key
            continue
        if hdr is not None and hdr[0] == "rules":
            in_rules = True
            last_header_key = None
            continue

        # Production line — anything that contains `->` outside the header block
        if "->" in raw:
            rules.append(raw.strip())
            in_rules = True
            last_header_key = None
            continue

        if not in_rules and last_header_key is not None:
            existing = fields.get(last_header_key, "")
            extra = raw.strip()
            fields[last_header_key] = (existing + " " + extra).strip() if existing else extra

    while rules and rules[-1] == "":
        rules.pop()

    out: list[str] = []
    for key in ("vars", "terminals", "start"):
        if key in fields:
            out.append(f"{key}: {fields[key]}")
    out.append("")
    out.append("rules:")
    out.extend(rules)
    out.append("")
    return "\n".join(out)


# ---------------------------------------------------------------------------
# REX converter
# ---------------------------------------------------------------------------

def convert_rex(text: str) -> str:
    lines = [ln for ln in text.splitlines() if ln.strip()]
    if not lines:
        return text  # leave empty files alone

    # Already-new form?
    headers: set[str] = set()
    for ln in lines:
        h = parse_header(ln)
        if h is not None:
            headers.add(h[0])
    if "pattern" in headers or "alphabet" in headers:
        # Make sure both keys exist; else best-effort pass-through
        return text if text.endswith("\n") else text + "\n"

    if len(lines) >= 2:
        pattern = lines[0].strip()
        alphabet = " ".join(lines[1].split())
    else:
        pattern = lines[0].strip()
        alphabet = ""

    return f"alphabet: {alphabet}\npattern: {pattern}\n"


# ---------------------------------------------------------------------------
# Dispatch
# ---------------------------------------------------------------------------

CONVERTERS = {
    ".dfa": lambda t: convert_fa(t, ".dfa"),
    ".nfa": lambda t: convert_fa(t, ".nfa"),
    ".pda": convert_pda,
    ".tm":  convert_tm,
    ".cfg": convert_cfg,
    ".rex": convert_rex,
}


# ---------------------------------------------------------------------------
# Validation
# ---------------------------------------------------------------------------

@dataclass
class Validator:
    jar_path: str | None
    enabled: bool

    def validate(self, path: Path) -> tuple[str, str]:
        if not self.enabled or not self.jar_path:
            return ("n/a", "")
        try:
            res = subprocess.run(
                ["java", "-cp", self.jar_path, "cli.SimpleValidator", str(path)],
                capture_output=True, text=True, timeout=20,
            )
        except subprocess.TimeoutExpired:
            return ("error", "validator timed out")
        except Exception as e:  # pragma: no cover
            return ("error", f"validator failed: {e}")
        if res.returncode == 0:
            return ("ok", "")
        first = (res.stderr or "").strip().splitlines()
        return ("error", first[0] if first else f"exit {res.returncode}")


# ---------------------------------------------------------------------------
# Main
# ---------------------------------------------------------------------------

@dataclass
class Stats:
    converted: int = 0
    copied: int = 0
    skipped: int = 0
    failed_convert: int = 0
    parse_ok: int = 0
    parse_err: int = 0
    parse_skipped: int = 0
    by_ext: dict[str, dict[str, int]] = field(default_factory=dict)


def process_file(src: Path, dst: Path, validator: Validator, stats: Stats) -> tuple[str, str, str]:
    ext = src.suffix.lower()
    bucket = stats.by_ext.setdefault(ext, {"converted": 0, "copied": 0, "ok": 0, "err": 0})

    if ext in PASSTHROUGH_EXTS:
        dst.parent.mkdir(parents=True, exist_ok=True)
        shutil.copyfile(src, dst)
        stats.copied += 1
        bucket["copied"] += 1
        return ("copied", "n/a", "")

    if ext not in CONVERTERS:
        # Not an automaton or test file — copy verbatim.
        dst.parent.mkdir(parents=True, exist_ok=True)
        shutil.copyfile(src, dst)
        stats.copied += 1
        bucket["copied"] += 1
        return ("copied", "n/a", "non-automaton file")

    text = read_text(src)
    if not text.strip():
        # Empty submission — preserve as-is.
        write_atomic(dst, text)
        stats.copied += 1
        bucket["copied"] += 1
        return ("copied", "n/a", "empty file")

    try:
        if is_already_new(text, ext):
            write_atomic(dst, text if text.endswith("\n") else text + "\n")
            action = "copied"
            stats.copied += 1
            bucket["copied"] += 1
        else:
            converted = CONVERTERS[ext](text)
            write_atomic(dst, converted)
            action = "converted"
            stats.converted += 1
            bucket["converted"] += 1
    except Exception as e:
        # Convert failed — copy original and flag.
        dst.parent.mkdir(parents=True, exist_ok=True)
        shutil.copyfile(src, dst)
        stats.failed_convert += 1
        return ("convert_failed", "n/a", f"{type(e).__name__}: {e}")

    parse_status, note = validator.validate(dst)
    if parse_status == "ok":
        stats.parse_ok += 1
        bucket["ok"] += 1
    elif parse_status == "error":
        stats.parse_err += 1
        bucket["err"] += 1
    else:
        stats.parse_skipped += 1
    return (action, parse_status, note)


def find_jar() -> str | None:
    matches = sorted(glob.glob(DEFAULT_JAR_GLOB))
    # Prefer the highest version (lexicographic sort works for 1.4.x naming).
    return matches[-1] if matches else None


def main() -> int:
    p = argparse.ArgumentParser()
    p.add_argument("--src", required=True)
    p.add_argument("--dst", required=True)
    p.add_argument("--no-validate", action="store_true")
    p.add_argument("--limit", type=int, default=0,
                   help="Process at most N files (0 = no limit). Useful for debugging.")
    p.add_argument("--only", action="append", default=[],
                   help="Restrict to one or more extensions (e.g. --only .dfa --only .pda).")
    p.add_argument("--jar", default=None,
                   help="Path to the SimpleValidator JAR. Defaults to newest target/CS410-Exam-*.jar.")
    args = p.parse_args()

    src_root = Path(args.src).resolve()
    dst_root = Path(args.dst).resolve()
    if not src_root.is_dir():
        print(f"src not found: {src_root}", file=sys.stderr)
        return 2
    if dst_root == src_root:
        print("dst must differ from src", file=sys.stderr)
        return 2

    jar = args.jar or find_jar()
    if not args.no_validate and not jar:
        print("No JAR found for validation. Run `mvn package` or pass --no-validate.",
              file=sys.stderr)
        return 2
    validator = Validator(jar_path=jar, enabled=not args.no_validate)

    only = {e.lower() if e.startswith(".") else f".{e.lower()}" for e in args.only}
    dst_root.mkdir(parents=True, exist_ok=True)
    report_path = dst_root / "_report.csv"

    stats = Stats()
    processed = 0
    with report_path.open("w", newline="", encoding="utf-8") as fh:
        writer = csv.writer(fh)
        writer.writerow(["relpath", "type", "action", "parse_status", "notes"])
        for src in sorted(src_root.rglob("*")):
            if not src.is_file():
                continue
            rel = src.relative_to(src_root)
            ext = src.suffix.lower()
            if only and ext not in only:
                continue
            if ext not in AUTOMATON_EXTS and ext not in PASSTHROUGH_EXTS:
                # Still mirror auxiliary files (non-recognized extensions)
                pass
            dst = dst_root / rel
            action, status, note = process_file(src, dst, validator, stats)
            writer.writerow([str(rel), ext, action, status, note])
            processed += 1
            if processed % 100 == 0:
                print(f"... {processed} files", file=sys.stderr)
            if args.limit and processed >= args.limit:
                break

    print()
    print(f"Processed: {processed}")
    print(f"  converted={stats.converted}  copied={stats.copied}  "
          f"skipped={stats.skipped}  convert_failed={stats.failed_convert}")
    print(f"Validation: ok={stats.parse_ok}  err={stats.parse_err}  "
          f"n/a={stats.parse_skipped}")
    print()
    print("Per-extension:")
    for ext in sorted(stats.by_ext):
        b = stats.by_ext[ext]
        total = b["converted"] + b["copied"]
        ok = b["ok"]; err = b["err"]
        rate = (ok / (ok + err) * 100) if (ok + err) else 0.0
        print(f"  {ext:6s}  total={total:4d}  converted={b['converted']:4d}  "
              f"copied={b['copied']:4d}  parse_ok={ok:4d}  parse_err={err:4d}  "
              f"ok_rate={rate:5.1f}%")
    print(f"\nReport: {report_path}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
