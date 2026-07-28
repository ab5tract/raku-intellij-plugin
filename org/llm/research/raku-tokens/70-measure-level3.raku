#!/usr/bin/env raku
use v6.d;
use lib $*PROGRAM.parent.add('lib').Str;
use BPE;

#| LEVEL 3 of the experiment: the fluency penalty.
#|
#| Levels 1 and 2 measure finished code -- what a byte costs to tokenize, and how
#| many tokens the final program takes. Neither counts the cost of *getting* to a
#| working program. This stage does: each arm is a fresh sub-agent that saw only a
#| task spec and one language, and every version it actually ran survives on disk
#| as attempt-NN.<ext> beside the stdout it produced.
#|
#| Design and blindness controls: level3/README.md.
#| The preregistered method: 00-preregistration.md, "Level 3".
#|
#| What is measured here is the tokens of the *code each arm emitted*, summed over
#| every attempt. That is not the arm's true token usage -- reasoning and tool
#| output are not exposed by the harness and are not counted. See level3/README.md.

my $root = $*PROGRAM.parent.add('level3/runs/run-01');
die "no run at $root" unless $root.d;

my $enc = BPE::cl100k();
note "loaded {$enc.name}";

sub attempts-in(IO::Path $dir, Str $ext) {
    $dir.dir(test => *.ends-with(".$ext")).sort(*.basename);
}

my @rows;

for <sonnet opus> -> $model {
    my $mdir = $root.add($model);
    next unless $mdir.d;
    for $mdir.dir(test => *.starts-with('l')).grep(*.d).sort({ +(.basename ~~ / ^ l (\d+) /)[0] }) -> $tdir {
        my $task = $tdir.basename;
        my $exp  = $root.add("expected/$task.out");
        my $want = $exp.e ?? $exp.slurp !! Nil;

        for (raku => 'raku', python => 'py') -> (:key($lang), :value($ext)) {
            my $dir = $tdir.add($lang);
            next unless $dir.d;
            my @src = attempts-in($dir, $ext);
            next unless @src;

            my $total-bytes  = @src.map({ .slurp.encode('utf-8').bytes }).sum;
            my $total-tokens = @src.map({ $enc.count(.slurp) }).sum;
            my $final        = @src.tail;

            sub out-of($f) {
                my $o = $f.parent.add($f.basename.subst(/ '.' $ext $ /, '.out'));
                $o.e ?? $o.slurp !! Nil;
            }
            my $first-out = out-of(@src.head);
            my $final-out = out-of($final);

            @rows.push: %(
                :$model, :$task, :$lang,
                attempts     => @src.elems,
                first-ok     => $want.defined && $first-out.defined && $first-out eq $want,
                final-ok     => $want.defined && $final-out.defined && $final-out eq $want,
                graded       => $want.defined,
                final-bytes  => $final.slurp.encode('utf-8').bytes,
                final-tokens => $enc.count($final.slurp),
                :$total-bytes, :$total-tokens,
            );
        }
    }
}

die "no arms found under $root" unless @rows;

my $tsv = $*PROGRAM.parent.add('96-level3.tsv').open(:w);
$tsv.say: join "\t", <model task lang attempts first_ok final_ok final_bytes final_tokens total_bytes total_tokens>;
for @rows -> %r {
    $tsv.say: join "\t", %r<model>, %r<task>, %r<lang>, %r<attempts>,
        (%r<first-ok> ?? 'yes' !! 'no'), (%r<final-ok> ?? 'yes' !! 'no'),
        %r<final-bytes>, %r<final-tokens>, %r<total-bytes>, %r<total-tokens>;
}
$tsv.close;

my $rep = $*PROGRAM.parent.add('97-level3-rollup.txt').open(:w);
sub emit($s = '') { say $s; $rep.say($s) }

emit "LEVEL 3 -- the fluency penalty: tokens to *working* code";
emit "vocabulary: cl100k_base (public proxy; not Claude's tokenizer)";
emit "counts emitted source across every attempt; NOT the arms' full token usage";
emit;

# Only arms that actually reached the correct answer can contribute to a
# "cost to reach working code" figure. Arms that never got there are reported
# separately rather than folded in, which would understate the penalty.
my @ok = @rows.grep(*<final-ok>);

emit "per model x language";
emit sprintf("%-8s %-7s %5s %6s %7s %7s %8s %9s %9s",
    'model', 'lang', 'arms', 'passed', '1st_ok', 'rounds', 'fin_tok', 'total_tok', 'waste');
emit '-' x 74;
for <sonnet opus> -> $model {
    for <raku python> -> $lang {
        my @c = @rows.grep({ .<model> eq $model && .<lang> eq $lang });
        next unless @c;
        my @p = @c.grep(*<final-ok>);
        my $ft = @p.map(*<final-tokens>).sum;
        my $tt = @p.map(*<total-tokens>).sum;
        emit sprintf("%-8s %-7s %5d %6d %6.0f%% %7.2f %8d %9d %8.2fx",
            $model, $lang, @c.elems, @p.elems,
            @c.grep(*<first-ok>).elems / @c.elems * 100,
            @p ?? @p.map({ .<attempts> - 1 }).sum / @p.elems !! 0,
            $ft, $tt, $ft ?? $tt / $ft !! 0);
    }
}
emit '-' x 74;
emit "1st_ok  = share of arms whose very first run already produced the exact answer";
emit "rounds  = mean corrections after the first attempt, over arms that passed";
emit "waste   = total emitted tokens / final program tokens (1.00x = right first time)";
emit;

emit "Raku vs Python, within each model (arms that reached the answer)";
emit '-' x 74;
for <sonnet opus> -> $model {
    my @r = @ok.grep({ .<model> eq $model && .<lang> eq 'raku'   });
    my @p = @ok.grep({ .<model> eq $model && .<lang> eq 'python' });
    next unless @r && @p;
    # Compare only tasks where BOTH arms of this model reached the answer,
    # otherwise the ratio is between different task sets.
    my $common = @r.map(*<task>).Set (&) @p.map(*<task>).Set;
    my @rc = @r.grep({ $common{.<task>} });
    my @pc = @p.grep({ $common{.<task>} });
    my ($rf, $pf) = @rc.map(*<final-tokens>).sum, @pc.map(*<final-tokens>).sum;
    my ($rt, $pt) = @rc.map(*<total-tokens>).sum, @pc.map(*<total-tokens>).sum;
    emit sprintf("%-8s %2d shared tasks   final %5.1f%% of Python   to-working %5.1f%% of Python",
        $model, $common.elems, $rf / $pf * 100, $rt / $pt * 100);
}
emit;
emit "Level 2 measured final code only and put Raku at 99.1% of Python's tokens.";
emit "The to-working column is the same quantity with every debug round counted.";
emit "H2 predicted Raku needs more correction rounds; compare the 1st_ok and";
emit "rounds columns across languages within a model, not across models.";
emit;

my @failed = @rows.grep({ .<graded> && !.<final-ok> });
if @failed {
    emit "arms that never reached the expected output (excluded from the ratios above)";
    emit '-' x 74;
    emit sprintf("  %-8s %-22s %-7s %d attempts", .<model>, .<task>, .<lang>, .<attempts>) for @failed;
} else {
    emit "every arm reached the expected output.";
}
my @ungraded = @rows.grep({ !.<graded> });
if @ungraded {
    emit;
    emit "UNGRADED -- no expected/<task>.out present for: "
        ~ @ungraded.map(*<task>).unique.sort.join(', ');
}

$rep.close;
note "wrote 96-level3.tsv, 97-level3-rollup.txt";
