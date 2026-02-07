states: q0 q1 q2 q3 q4 q5 q6 q_accept q_reject
input: 0 1
tape: 0 1 X _
start: q0
accept: q_accept
reject: q_reject
transitions:
# Initial state - read first character
q0, 0 -> q1, X, R
q0, 1 -> q2, X, R
q0, X -> q0, X, R
q0, _ -> q_accept, _, R

# Scanned 0 - find rightmost unprocessed
q1, 0 -> q1, 0, R
q1, 1 -> q1, 1, R
q1, X -> q3, X, L
q1, _ -> q3, _, L

# Scanned 1 - find rightmost unprocessed
q2, 0 -> q2, 0, R
q2, 1 -> q2, 1, R
q2, X -> q4, X, L
q2, _ -> q4, _, L

# Looking for last char to match 0
q3, X -> q3, X, L
q3, 0 -> q5, X, L
q3, 1 -> q_reject, 1, L
q3, _ -> q_accept, _, R

# Looking for last char to match 1
q4, X -> q4, X, L
q4, 1 -> q5, X, L
q4, 0 -> q_reject, 0, L
q4, _ -> q_accept, _, R

# Return to start
q5, 0 -> q5, 0, L
q5, 1 -> q5, 1, L
q5, X -> q5, X, L
q5, _ -> q6, _, R

# Find first unprocessed
q6, X -> q6, X, R
q6, 0 -> q0, 0, L
q6, 1 -> q0, 1, L
q6, _ -> q_accept, _, L
