states: q0 q1 q2 q3 q4 q5 q_accept
input: a b
tape: a b _
start: q0
accept: q_accept

transitions:
q0, a -> q1, _, R
q0, b -> q3, _, R
q0, _ -> q_accept, _, R

q1, a -> q1, a, R
q1, b -> q1, b, R
q1, _ -> q2, _, L

q2, a -> q5, _, L

q3, a -> q3, a, R
q3, b -> q3, b, R
q3, _ -> q4, _, L

q4, b -> q5, _, L

q5, a -> q5, a, L
q5, b -> q5, b, L
q5, _ -> q0, _, R
