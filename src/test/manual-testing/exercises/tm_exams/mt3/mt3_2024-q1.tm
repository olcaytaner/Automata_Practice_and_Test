states: q0 q1 q2 q3 q4 q5 q6 q7 q_accept q_reject
input: a b c d
tape: a b c d X Y Z W _
start: q0
accept: q_accept
reject: q_reject

transitions:
q0, a -> q1, X, R
q0, b -> q4, Z, R
q1, a -> q1, a, R
q1, b -> q1, b, R
q1, c -> q1, c, R
q1, d -> q1, d, R
q1, Z -> q1, Z, R
q1, W -> q1, W, R
q1, Y -> q2, Y, L
q1, _ -> q2, _, L
q2, d -> q3, Y, L
q3, a -> q3, a, L
q3, b -> q3, b, L
q3, c -> q3, c, L
q3, d -> q3, d, L
q3, Z -> q3, Z, L
q3, W -> q3, W, L
q3, X -> q0, X, R
q4, b -> q4, b, R
q4, W -> q4, W, R
q4, c -> q5, W, L
q5, b -> q5, b, L
q5, W -> q5, W, L
q5, Z -> q6, Z, R
q6, b -> q4, Z, R
q6, W -> q7, W, R
q7, W -> q7, W, R
q7, Y -> q_accept, Y, R
