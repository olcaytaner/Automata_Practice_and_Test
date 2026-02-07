states: q0 q1 q2 q3 q4 q5 q6 q7 q8 q9 q10 q_accept q_reject
input: 0 1 2 3
tape: 0 1 2 3 x y w z _
start: q0
accept: q_accept
reject: q_reject

transitions:
q0, 0 -> q1, x, R
q1, 0 -> q1, 0, R
q1, 1 -> q1, 1, R
q1, 2 -> q1, 2, R
q1, z -> q1, z, R
q1, 3 -> q2, z, L
q2, 0 -> q2, 0, L
q2, 1 -> q2, 1, L
q2, 2 -> q2, 2, L
q2, z -> q2, z, L
q2, x -> q3, x, R
q3, 0 -> q1, x, R
q3, 1 -> q4, y, R
q4, 1 -> q4, 1, R
q4, 2 -> q4, 2, R
q4, z -> q4, z, R
q4, 3 -> q5, z, L
q5, 1 -> q5, 1, L
q5, 2 -> q5, 2, L
q5, z -> q5, z, L
q5, y -> q6, y, R
q6, 1 -> q4, y, R
q6, 2 -> q7, w, R
q7, 2 -> q7, 2, R
q7, z -> q7, z, R
q7, 3 -> q8, z, L
q8, 2 -> q8, 2, L
q8, z -> q8, z, L
q8, w -> q9, w, R
q9, 2 -> q7, w, R
q9, z -> q10, z, R
q10, z -> q10, z, R
q10, _ -> q_accept, _, R
