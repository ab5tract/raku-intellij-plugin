use v6.d;

#| Corpus root resolution, so the measured data is portable.
#|
#| Two of the corpora live outside the repo and are machine-specific in kind,
#| not just in path: the Python standard library moves with the OS's Python
#| version, and the Raku ecosystem lives under whichever Rakudo rakubrew has
#| selected. Recording absolute paths in the results would make the data
#| unreadable on any other machine, and `org/llm/` exists precisely because it
#| travels.
#|
#| So every root is *resolved at run time* and every recorded path is relative
#| to its root. A re-run elsewhere resolves different roots, measures whatever
#| that machine actually has, and `60-verify-corpus.raku` reports the delta
#| against what was recorded here rather than pretending the two match.
unit module Corpus;

#| Walk up from a starting point until the repo root is found. Counting `..`
#| segments breaks the moment a script moves one directory.
our sub repo-root(IO::Path $from = $?FILE.IO --> IO::Path) {
    my $d = $from.absolute.IO;
    $d = $d.parent while $d && !$d.add('settings.gradle.kts').e && $d.parent ne $d;
    die "could not locate repo root above {$from}" unless $d.add('settings.gradle.kts').e;
    $d;
}

#| Python standard library. Prefers CORPUS_PYTHON_STDLIB, else picks the
#| install with the most top-level modules -- the version number differs
#| per machine and there is no point pinning it.
#|
#| Selection must be *deterministic*, which is fiddlier than it looks. `.dir`
#| returns filesystem order, and `/usr/lib64` is commonly a symlink to
#| `/usr/lib`, so a naive `.max` picked a different-but-equivalent root between
#| runs; the paths then sorted differently, the seeded sample drew different
#| files, and the headline figure moved by 0.1 points. Same family of bug as
#| `.pick(:seed)` being silently ignored: output that looks reproducible and
#| is not. Symlinks are collapsed with `.resolve` and ties broken on path.
our sub python-stdlib(--> IO::Path) {
    with %*ENV<CORPUS_PYTHON_STDLIB> { return .IO.resolve if .IO.d }
    my @candidates = flat </usr/lib /usr/lib64 /usr/local/lib>.map({
        .IO.d ?? .IO.dir(test => *.starts-with('python3')).grep(*.d) !! ()
    });
    my @ranked = @candidates
        .map(*.resolve)
        .unique(:as(*.absolute))
        .map({ $_ => .dir(test => *.ends-with('.py')).elems })
        .grep(*.value > 50)
        .sort({ (-.value, .key.absolute) });
    @ranked ?? @ranked[0].key !! IO::Path;
}

#| Third-party Raku modules, under whichever Rakudo is *running* -- which is
#| the one the rakubrew preamble selected, so this follows it automatically.
our sub raku-ecosystem(--> IO::Path) {
    with %*ENV<CORPUS_RAKU_ECOSYSTEM> { return .IO.resolve if .IO.d }
    my $p = $*EXECUTABLE.parent.parent.add('share/perl6/site/sources');
    $p.d ?? $p.resolve !! IO::Path;
}

#| FNV-1a, 32-bit. A content fingerprint with no module dependency, enough to
#| answer "is this the same file as the one that was measured?". Not a
#| cryptographic hash and not used as one.
#|
#| 32-bit rather than 64 because the 64-bit offset basis (0xcbf29ce484222325)
#| exceeds Raku's *signed* native int and dies with "Cannot unbox 64 bit wide
#| bigint". Falling back to Int arithmetic would work but drags bigint maths
#| through a per-byte loop over the whole corpus. At a few thousand files,
#| 32 bits is ample for detecting that a file changed.
our sub digest(Str $text --> Str) {
    my int $h = 0x811c9dc5;
    for $text.encode('utf-8').list -> int $b {
        $h = $h +^ $b;
        $h = ($h * 0x01000193) +& 0xFFFFFFFF;
    }
    $h.base(16).lc.fmt('%08s').subst(' ', '0', :g);
}

#| Path relative to its root, always with `/` separators so the recorded value
#| does not depend on the host's directory separator.
our sub relative-to(IO::Path $file, IO::Path $root --> Str) {
    my $f = $file.absolute;
    my $r = $root.absolute.chomp('/') ~ '/';
    $f.starts-with($r) ?? $f.substr($r.chars) !! $f;
}
