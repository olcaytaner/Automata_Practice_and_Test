states: q0 q1 q_accept q_reject
input: 0 1
tape: 0 1 _
start: q0
accept: q_accept
reject: q_reject
transitions:
q0, 0 -> q1, 0, R
q0, 1 -> q0, 1, R
q0, _ -> q_accept, _, R
q1, 0 -> q0, 0, R
q1, 1 -> q1, 1, R
q1, _ -> q_reject, _, R
