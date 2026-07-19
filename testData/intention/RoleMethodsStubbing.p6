role R { method foo($a) {...}; method bar($a) {...} };
class C does R {
    method bar($a) {...}
    method foo($a) {...}
}