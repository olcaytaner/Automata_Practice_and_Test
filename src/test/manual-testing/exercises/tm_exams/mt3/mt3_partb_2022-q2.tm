states: q0 q1 q2 q3 q4 q5 q6 q_accept q_reject
input: 0 1 2 3
tape: 0 1 2 3 x y z w _
start: q0
accept: q_accept
reject: q_reject

transitions:
q0, 0 -> q1, x, R
q0, 1 -> q4, y, R
q0, x -> q0, x, R

q1, 0 -> q1, 0, R
q1, 1 -> q1, 1, R
q1, z -> q1, z, R
q1, 2 -> q2, z, L

q2, 2 -> q2, 2, L
q2, z -> q2, z, L
q2, 1 -> q2, 1, L
q2, 0 -> q2, 0, L
q2, x -> q0, x, R

q3, 1 -> q4, y, R
q3, z -> q6, z, R
q3, y -> q3, y, R

q4, 1 -> q4, 1, R
q4, z -> q4, z, R
q4, w -> q4, w, R
q4, 3 -> q5, w, L

q5, 3 -> q5, 3, L
q5, w -> q5, w, L
q5, z -> q5, z, L
q5, 1 -> q5, 1, L
q5, y -> q3, y, R

q6, z -> q6, z, R
q6, w -> q6, w, R
q6, _ -> q_accept, _, R
