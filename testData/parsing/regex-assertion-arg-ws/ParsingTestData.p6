grammar G {
    token t {
        <.malformed: "tight">
        <.malformed:
          "multi-line arg with interpolation {$exprs}"
        >
        [
        || <?{ $exprs == 0 }> ')'
        || <.malformed: "spaces before close"   >
        ]
        <?before ‘;’>
    }
}
999999;
