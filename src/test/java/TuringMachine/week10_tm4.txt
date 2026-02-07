states: q1 q2 q3 q4 q_accept
input: 1
tape: 1 x $ _
start: q1
accept: q_accept

transitions:
q1, 1 -> q2, $, R
q1, _ -> q_accept, _, R

q2, 1 -> q2, x, R
q2, _ -> q3, 1, L

q3, 1 -> q3, 1, L
q3, $ -> q_accept, 1, R
q3, x -> q4, 1, R

q4, 1 -> q4, 1, R
q4, _ -> q3, 1, L
