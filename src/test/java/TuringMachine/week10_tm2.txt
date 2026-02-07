states: q0 q1 q2 q3 q_accept
input: 0 1
tape: 0 1 x y _
start: q0
accept: q_accept


transitions:
q0, 0 -> q1, x, R
q0, y -> q3, y, R

q1, 0 -> q1, 0, R
q1, y -> q1, y, R
q1, 1 -> q2, y, L

q2, 0 -> q2, 0, L
q2, y -> q2, y, L
q2, x -> q0, x, R

q3, y -> q3, y, R
q3, _ -> q_accept, _, L
