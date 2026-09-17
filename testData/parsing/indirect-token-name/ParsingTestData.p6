my role Circumfix[$meth_name, $starter, $stopper] {
    token ::($meth_name) {
        :my $*GOAL := $stopper;
        $starter ~ $stopper <semilist>
    }
    rule ::($meth_name) { 'x' }
    token normal($sig) { 'y' }
}
999999;
