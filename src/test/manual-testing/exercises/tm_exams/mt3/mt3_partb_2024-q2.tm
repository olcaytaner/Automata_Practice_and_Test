states: q0 q1 q2 q3 q4 q5 q6 q7 q_accept q_reject
input: a b
tape: a b x y z _
start: q0
accept: q_accept
reject: q_reject

transitions:
q0, a -> q1, x, R
q0, b -> q_reject, b, R
q0, _ -> q_reject, _, R
q1, a -> q1, a, R
q1, z -> q1, z, R
q1, y -> q2, y, R
q1, b -> q3, y, L
q1, _ -> q6, _, L
q2, y -> q2, y, R
q2, b -> q3, y, L
q2, _ -> q6, _, L
q2, a -> q_reject, a, R
q3, a -> q3, a, L
q3, z -> q4, z, R
q3, y -> q3, y, L
q3, x -> q4, x, R
q4, a -> q1, z, R
q4, y -> q5, y, L
q4, _ -> q5, _, L
q5, z -> q5, a, L
q5, a -> q5, a, L
q5, y -> q5, y, L
q5, x -> q1, x, R
q6, a -> q6, a, L
q6, z -> q_reject, z, R
q6, y -> q7, y, L
q6, x -> q_reject, x, R
q7, a -> q7, a, L
q7, z -> q_reject, z, R
q7, y -> q7, y, L
q7, x -> q_accept, x, R
