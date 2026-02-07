states: q0 q1 q2 q3 q4 q5 q6 q7 q8 q9 q10 q11 q12 q13 q_accept q_reject
input: 0 1
tape: 0 1 2 $ x y _
start: q0
accept: q_accept
reject: q_reject

transitions:
q0, 0 -> q1, $, R

q1, 0 -> q2, x, R
q1, 1 -> q4, 1, L

q2, 0 -> q2, 0, R
q2, 1 -> q2, 1, R
q2, 2 -> q2, 2, R
q2, _ -> q3, 2, L

q3, 2 -> q3, 2, L
q3, 1 -> q3, 1, L
q3, 0 -> q3, 0, L
q3, x -> q1, x, R

q4, x -> q4, 0, L
q4, $ -> q5, $, R

q5, 0 -> q5, 0, R
q5, y -> q5, y, R
q5, 1 -> q6, y, L

q6, 0 -> q6, 0, L
q6, y -> q6, y, L
q6, $ -> q7, $, R

q7, 0 -> q8, x, R
q7, y -> q10, y, R

q8, 0 -> q8, 0, R
q8, y -> q8, y, R
q8, 1 -> q9, y, L

q9, 0 -> q9, 0, L
q9, y -> q9, y, L
q9, x -> q7, x, R

q10, y -> q10, y, R
q10, _ -> q_accept, _, R
q10, 1 -> q11, 1, R

q11, 1 -> q11, 1, R
q11, 2 -> q11, 2, R
q11, _ -> q12, _, L

q12, 2 -> q13, _, L
q12, 1 -> q_reject, 1, L

q13, 2 -> q13, 2, L
q13, 1 -> q13, 1, L
q13, y -> q13, y, L
q13, x -> q4, 0, L
