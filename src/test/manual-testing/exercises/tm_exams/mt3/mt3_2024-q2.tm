states: q0 q1 q2 q3 q4 q5 q_accept q_reject
input: a b c
tape: a b c x y z _
start: q0
accept: q_accept
reject: q_reject

transitions:
q0, a -> q1, x, R
q0, y -> q4, y, R
q1, a -> q1, a, R
q1, y -> q1, y, R
q1, b -> q2, y, R
q2, b -> q2, b, R
q2, z -> q2, z, R
q2, c -> q3, z, L
q3, z -> q3, z, L
q3, b -> q3, b, L
q3, y -> q3, y, L
q3, a -> q3, a, L
q3, x -> q0, x, R
q4, y -> q4, y, R
q4, z -> q5, z, R
q5, z -> q5, z, R
q5, _ -> q_accept, _, R
