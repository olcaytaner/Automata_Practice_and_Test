states: q0 q1 q2 q3 q4 q5 q6 q_accept q_reject
input: 0 1 $
tape: 0 1 $ x y a b _
start: q0
accept: q_accept
reject: q_reject

transitions:
q0, x -> q0, x, R
q0, a -> q0, a, R
q0, 0 -> q1, x, R
q0, 1 -> q2, a, R
q0, $ -> q6, $, R
q1, 0 -> q1, 0, R
q1, 1 -> q1, 1, R
q1, $ -> q3, $, R
q2, 0 -> q2, 0, R
q2, 1 -> q2, 1, R
q2, $ -> q4, $, R
q3, y -> q3, y, R
q3, b -> q3, b, R
q3, 0 -> q3, 0, R
q3, 1 -> q5, y, L
q4, y -> q4, y, R
q4, b -> q4, b, R
q4, 1 -> q4, 1, R
q4, 0 -> q5, b, L
q5, 0 -> q5, 0, L
q5, 1 -> q5, 1, L
q5, y -> q5, y, L
q5, b -> q5, b, L
q5, $ -> q5, $, L
q5, x -> q0, x, R
q5, a -> q0, a, R
q6, y -> q6, y, R
q6, b -> q6, b, R
q6, _ -> q_accept, _, R
