use JSON::Fast;
use Test;

plan *;

constant $MODULE-SCRIPT = '../../resources/symbols/perl6-module-symbols.p6';

subtest {
    my $proc = run $*EXECUTABLE, $MODULE-SCRIPT, 'use NativeCall', :out;
    my $output = $proc.out.slurp(:close);
    my $data;
    lives-ok { $data = from-json $output }, "Can parse json";
    # Subs, constants, types
    ok $data.first({ $_<n> eq "explicitly-manage" && $_<k> eq "s" && $_<m> == 1 }), 'Subroutine is exported';
    # Roles
    my $native = $data.first({ $_<n> eq "NativeCall::Native" });
    ok $native, 'Native role is exported';
    ok $native<a>.first({ $_<n> eq '$!rettype' && $_<t> eq "Mu" }), 'Native private attributes are exported';
    ok $native<m>.first({ $_<n> eq '!setup' && $_<k> eq 'm' && $_<m> == 0 && $_<s><r> eq 'Mu' }), 'Native role methods are exported';
    # Classes
    $native = $data.first({ $_<n> eq 'CArray' });
    ok $native, 'CArray class is exported';
    ok $native<m>.first({ $_<n> eq 'AT-POS' }), 'CArray class methods are exported';
    # Exports
    subtest {
      ok $data.first({ $_<n> eq 'Pointer' });
      ok $data.first({ $_<n> eq 'OpaquePointer' });
      ok $data.first({ $_<n> eq 'NativeCall::Types::Pointer' });
      ok $data.first({ $_<n> eq 'NativeCall::EXPORT::types::Pointer' });
      ok $data.first({ $_<n> eq 'NativeCall::EXPORT::DEFAULT::Pointer' });
      ok $data.first({ $_<n> eq 'NativeCall::EXPORT::ALL::Pointer' });
    }, 'Various symbols to access Pointer are exported';
}, 'Module script test';

subtest {
    my $proc = run $*EXECUTABLE, $MODULE-SCRIPT, 'use SymbolScriptModule', :out;
    my $output = $proc.out.slurp(:close);
    my $data;
    lives-ok { $data = from-json $output }, 'Can parse json';
    my $variable = $data.first(*.<n> eq '$test-variable');
    ok $variable<n> eq '$test-variable';
    ok $variable<k> eq 'v';
    ok $variable<t> eq 'Any';

    my $class = $data.first(*.<n> eq 'Foo');
    ok $class eqv ${:a($[{:d("one\none"), :k("v"), :n("\$.one"), :t("Mu")},]), :b("A"), :d("Zero line. First line. Second line. Foo"), :k("c"), :m($[{:d("m\nm"), :k("m"), :m(0), :n("m"), :s(${:p($[{:n("\$foo"), :t("Int")}, {:n("*\%_"), :nn($[]), :t("Mu")}]), :r("Mu")})}, {:k("m"), :m(0), :n("impl"), :rakudo(Bool::True), :s(${:p($[{:n("*\%_"), :nn($[]), :t("Mu")},]), :r("Mu")})}, {:k("m"), :m(0), :n("one"), :s(${:p($[{:n("*\%_"), :nn($[]), :t("Mu")},]), :r("Mu")})}, {:k("s"), :m(0), :n("BUILDALL"), :s(${:p($[{:n("\@auto"), :t("Any")}, {:n("\%init"), :t("Any")}, {:n("*\%_"), :nn($[]), :t("Mu")}]), :r("Mu")})}]), :mro($["Any"]), :n("Foo"), :t("Foo")};
}

done-testing;
