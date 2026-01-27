package ContextFreeGrammar;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import common.Automaton;
import common.ExecutionResult;
import common.MachineType;
import common.ParseResult;
import common.Symbol;
import common.ValidationMessage;

/**
 * Represents a Context-Free Grammar (CFG) with variables, terminals, productions, and a start symbol.
 * This class provides functionality to validate the grammar, manipulate productions, format output,
 * and parse CFG definition files.
 *
 * @author yenennn
 * @version 2.0
 */
public class CFG extends Automaton {
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("^[A-Z][A-Za-z0-9]*$");
    private static final Pattern TERMINAL_PATTERN = Pattern.compile("^[a-z0-9]+$");
    private static final int MAX_LINES = 200;

    private Set<NonTerminal> variables;
    private Set<Terminal> terminals;
    private List<Production> productions;
    private NonTerminal startSymbol;

    // Performance optimizations
    private Map<String, NonTerminal> variablesByName;
    private Map<String, Terminal> terminalsByName;
    private Map<NonTerminal, List<Production>> productionsByLeft;
    private CFG cachedCNF;
    private Set<String> terminalNames;
    private Set<String> variableNames;
    private Map<String, List<Production>> productionsByTerminal;  // "a" -> [S -> a, ...]
    private Map<String, List<Production>> productionsByBinaryPair;  // "AB" -> [S -> A B, ...]
    private String grammarStringCache;
    private Map<Long, List<Production>> productionsByBinaryPairInt;
    private Map<NonTerminal, Integer> nonTerminalToId;
    // Add these fields
    private int[] nonTerminalIds;  // indexed by NonTerminal's internal id
    private int[][] productionResultsForPair;  // [pairKey] -> array of result NT ids
    private int[][] productionResultsForTerminal;  // [terminalIndex] -> array of result NT ids
    private int numNonTerminals;
    private int[][] binaryProductionResults;
    private int startSymbolId;
    private Map<String, int[]> terminalProductionResults;  // "a" -> array of result NT ids

    public CFG() {
        super(MachineType.CFG);
        this.variables = new HashSet<>();
        this.terminals = new HashSet<>();
        this.productions = new ArrayList<>();
        this.variablesByName = new HashMap<>();
        this.terminalsByName = new HashMap<>();
        this.productionsByLeft = new HashMap<>();
        this.grammarStringCache = null;
        this.cachedCNF = null;
    }

    public CFG(Set<NonTerminal> variables,
               Set<Terminal> terminals,
               List<Production> productions,
               NonTerminal startSymbol) {
        super(MachineType.CFG);
        this.variables = variables;
        this.terminals = terminals;
        this.productions = new ArrayList<>(productions);
        this.startSymbol = startSymbol;
        this.variablesByName = new HashMap<>();
        this.terminalsByName = new HashMap<>();
        this.productionsByLeft = new HashMap<>();
        initializeMaps();
    }

    private void initializeMaps() {
        variablesByName.clear();
        terminalsByName.clear();
        productionsByLeft.clear();
        productionsByTerminal = new HashMap<>();
        productionsByBinaryPair = new HashMap<>();
        variableNames = new HashSet<>();
        terminalNames = null;
        cachedCNF = null;

        for (NonTerminal var : variables) {
            variablesByName.put(var.getName(), var);
            variableNames.add(var.getName());
        }

        for (Terminal term : terminals) {
            terminalsByName.put(term.getName(), term);
        }

        // Build integer ID map for non-terminals
        nonTerminalToId = new HashMap<>();
        int id = 0;
        for (NonTerminal nt : variables) {
            nonTerminalToId.put(nt, id++);
        }
        numNonTerminals = id;

        if (startSymbol != null) {
            Integer sId = nonTerminalToId.get(startSymbol);
            startSymbolId = (sId != null) ? sId : -1;
        }

        // Build array-based binary production results
        int size = numNonTerminals * numNonTerminals;
        binaryProductionResults = new int[size][];

        // Build terminal production results
        Map<String, List<Integer>> termResultsBuilder = new HashMap<>();

        for (Production prod : productions) {
            productionsByLeft.computeIfAbsent(prod.getLeft(), k -> new ArrayList<>()).add(prod);

            if (prod.getRight().size() == 1 && prod.getRight().get(0) instanceof Terminal) {
                String termName = prod.getRight().get(0).getName();
                productionsByTerminal.computeIfAbsent(termName, k -> new ArrayList<>()).add(prod);

                Integer resultId = nonTerminalToId.get(prod.getLeft());
                if (resultId != null) {
                    termResultsBuilder.computeIfAbsent(termName, k -> new ArrayList<>()).add(resultId);
                }
            } else if (prod.getRight().size() == 2) {
                Symbol first = prod.getRight().get(0);
                Symbol second = prod.getRight().get(1);

                if (first instanceof NonTerminal && second instanceof NonTerminal) {
                    String pair = first.getName() + second.getName();
                    productionsByBinaryPair.computeIfAbsent(pair, k -> new ArrayList<>()).add(prod);

                    Integer leftId = nonTerminalToId.get(first);
                    Integer rightId = nonTerminalToId.get(second);
                    if (leftId != null && rightId != null) {
                        int key = leftId * numNonTerminals + rightId;
                        Integer resultId = nonTerminalToId.get(prod.getLeft());

                        if (resultId != null) {
                            if (binaryProductionResults[key] == null) {
                                binaryProductionResults[key] = new int[]{resultId};
                            } else {
                                int[] old = binaryProductionResults[key];
                                int[] newArr = new int[old.length + 1];
                                System.arraycopy(old, 0, newArr, 0, old.length);
                                newArr[old.length] = resultId;
                                binaryProductionResults[key] = newArr;
                            }
                        }
                    }
                }
            }
        }

        // Convert terminal results to arrays
        terminalProductionResults = new HashMap<>();
        for (Map.Entry<String, List<Integer>> entry : termResultsBuilder.entrySet()) {
            terminalProductionResults.put(entry.getKey(), entry.getValue().stream().mapToInt(i -> i).toArray());
        }
    }

    public void initializeCache() {
        this.grammarStringCache = grammarToString();
    }

    public Set<NonTerminal> getVariables() {
        return variables;
    }

    public void setVariables(Set<NonTerminal> variables) {
        this.variables = variables;
        initializeMaps();
    }

    public Set<Terminal> getTerminals() {
        return terminals;
    }

    public void setTerminals(Set<Terminal> terminals) {
        this.terminals = terminals;
        initializeMaps();
    }

    public List<Production> getProductions() {
        return productions;
    }

    public void setProductions(List<Production> productions) {
        this.productions = productions;
        this.grammarStringCache = null;  // Invalidate cache
        this.cachedCNF = null;
        initializeMaps();
    }



    public NonTerminal getStartSymbol() {
        return startSymbol;
    }

    public void setStartSymbol(NonTerminal startSymbol) {
        this.startSymbol = startSymbol;
    }



    public static CFG parseFromString(String inputText) {
        Set<NonTerminal> variables = new HashSet<>();
        Set<Terminal> terminals = new HashSet<>();
        NonTerminal startSymbol = null;
        List<Production> productions = new ArrayList<>();
        int lineNumber = 0;
        boolean variablesHeaderFound = false;
        boolean terminalsHeaderFound = false;

        try (BufferedReader reader = new BufferedReader(new StringReader(inputText))) {
            String line;

            while ((line = reader.readLine()) != null) {
                lineNumber++;

                if (lineNumber > MAX_LINES) {
                    throw new GrammarParseException("Input exceeds maximum line limit (" + MAX_LINES +
                            " lines). Check for malformed production rules or infinite loops.");
                }
                line = normalizeWhitespace(line);
                line = line.trim();

                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                try {
                    if (line.toLowerCase().startsWith("variables =")) {
                        variablesHeaderFound = true;
                        String[] varNames = line.substring("Variables =".length()).trim().split("\\s+");
                        if (varNames.length == 1 && varNames[0].isEmpty()) {
                            throw new GrammarParseException("No variables specified");
                        }
                        for (String name : varNames) {
                            if (!name.isEmpty()) {
                                if (!name.equals(name.toUpperCase())) {
                                    throw new GrammarParseException("Variable '" + name + "' must be uppercase");
                                }
                                variables.add(new NonTerminal(name));
                            }
                        }
                    } else if (line.toLowerCase().startsWith("terminals =")) {
                        terminalsHeaderFound = true;
                        String[] termNames = line.substring("Terminals =".length()).trim().split("\\s+");
                        if (termNames.length == 1 && termNames[0].isEmpty()) {
                            throw new GrammarParseException("No terminals specified");
                        }
                        for (String name : termNames) {
                            if (!name.isEmpty()) {
                                if (!name.equals(name.toLowerCase())) {
                                    throw new GrammarParseException("Terminal '" + name + "' must be lowercase");
                                }
                                terminals.add(new Terminal(name));
                            }
                        }
                    } else if (line.toLowerCase().startsWith("start =")) {
                        String startName = line.substring("Start =".length()).trim();
                        if (startName.isEmpty()) {
                            throw new GrammarParseException("No start symbol specified");
                        }
                        startSymbol = new NonTerminal(startName);
                    } else if (line.contains("->")) {
                        parseProduction(line, variables, terminals, productions);
                    } else if (line.contains("->") == false && line.contains("-") && line.contains(">")) {
                        throw new GrammarParseException("Arrow typo detected at line " + lineNumber + ": Did you mean '->'?");
                    } else {
                        throw new GrammarParseException("Unrecognized line format");
                    }
                } catch (GrammarParseException e) {
                    throw new GrammarParseException("Error at line " + lineNumber + ": " + e.getMessage() + "\nLine content: " + line);
                }
            }
        } catch (IOException e) {
            throw new GrammarParseException("Error reading input: " + e.getMessage());
        }

        if (!variablesHeaderFound) {
            throw new GrammarParseException("Variables missing");
        }
        if (!terminalsHeaderFound) {
            throw new GrammarParseException("Terminals missing");
        }

        if (variables.isEmpty()) {
            throw new GrammarParseException("No variables defined in the grammar");
        }
        if (terminals.isEmpty()) {
            throw new GrammarParseException("No terminals defined in the grammar");
        }
        if (startSymbol == null) {
            throw new GrammarParseException("No start symbol defined in the grammar");
        }
        if (productions.isEmpty()) {
            throw new GrammarParseException("No productions defined in the grammar");
        }

        NonTerminal finalStartSymbol = startSymbol;
        boolean startExists = variables.stream().anyMatch(var -> var.getName().equals(finalStartSymbol.getName()));

        if (!startExists) {
            throw new GrammarParseException("Start symbol '" + startSymbol.getName() + "' is not defined in Variables");
        }

        startSymbol = variables.stream().filter(var -> var.getName().equals(finalStartSymbol.getName())).findFirst().get();

        return new CFG(variables, terminals, productions, startSymbol);
    }

    private static void parseProduction(String line, Set<NonTerminal> variables,
                                        Set<Terminal> terminals, List<Production> productions) {
        String[] parts = line.split("->");
        if (parts.length != 2) {
            throw new GrammarParseException("Invalid production format, missing '->' operator or has too many");
        }

        String leftSide = parts[0].trim();
        if (leftSide.isEmpty()) {
            throw new GrammarParseException("Left side of production is empty");
        }

        NonTerminal left = findNonTerminal(leftSide, variables);
        if (left == null) {
            throw new GrammarParseException("Undefined variable '" + leftSide + "' in production");
        }

        String[] alternatives = parts[1].trim().split("\\|");
        if (alternatives.length == 0 || (alternatives.length == 1 && alternatives[0].trim().isEmpty())) {
            throw new GrammarParseException("Right side of production is empty");
        }

        for (String alternative : alternatives) {
            alternative = alternative.trim();

            if (alternative.equals("eps")) {
                productions.add(new Production(left, Collections.emptyList()));
                continue;
            }

            List<Symbol> rightSide = new ArrayList<>();
            if (!alternative.isEmpty()) {
                String[] symbols = alternative.split("\\s+");
                for (String symbol : symbols) {
                    if (symbol.equals("eps")) {
                        throw new GrammarParseException("Epsilon 'eps' must appear alone on the right-hand side");
                    }
                    Symbol s = findSymbol(symbol, variables, terminals);
                    if (s != null) {
                        rightSide.add(s);
                    } else {
                        throw new GrammarParseException("Undefined symbol '" + symbol + "' in production");
                    }
                }
            }

            productions.add(new Production(left, rightSide));
        }
    }

    private static NonTerminal findNonTerminal(String name, Set<NonTerminal> variables) {
        return variables.stream()
                .filter(var -> var.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    private static Symbol findSymbol(String name, Set<NonTerminal> variables, Set<Terminal> terminals) {
        NonTerminal var = findNonTerminal(name, variables);
        if (var != null) {
            return var;
        }

        return terminals.stream()
                .filter(term -> term.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    public CFG toChomskyNormalForm() {
        if (cachedCNF != null) {
            return cachedCNF;
        }

        Set<NonTerminal> newVariables = new HashSet<>(variables);
        Set<Terminal> newTerminals = new HashSet<>(terminals);
        List<Production> newProductions = new ArrayList<>(productions);
        NonTerminal newStartSymbol = startSymbol;

        NonTerminal originalStart = startSymbol;
        NonTerminal newStart = generateUniqueStartSymbol(newVariables);

        newVariables.add(newStart);
        newProductions.add(new Production(newStart, Arrays.asList(originalStart)));
        newStartSymbol = newStart;

        newProductions = eliminateEpsilonProductions(newProductions, newVariables);
        newProductions = eliminateUnitProductions(newProductions, newVariables);
        newProductions = convertToCNFFormat(newProductions, newVariables, newTerminals);

        cachedCNF = new CFG(newVariables, newTerminals, newProductions, newStartSymbol);
        return cachedCNF;
    }

    private NonTerminal generateUniqueStartSymbol(Set<NonTerminal> variables) {
        String name = "S'";
        while (variableExists(variables, name)) {
            name = name + "'";
        }
        return new NonTerminal(name);
    }

    private boolean variableExists(Set<NonTerminal> variables, String name) {
        return variableNames.contains(name);
    }

    private List<Production> eliminateEpsilonProductions(List<Production> productions, Set<NonTerminal> variables) {
        Set<NonTerminal> nullable = findNullableVariables(productions);
        List<Production> newProductions = new ArrayList<>();

        for (Production p : productions) {
            if (p.getRight().isEmpty()) {
                continue;
            }

            boolean hasNullable = p.getRight().stream()
                    .anyMatch(symbol -> symbol instanceof NonTerminal && nullable.contains(symbol));

            if (!hasNullable) {
                newProductions.add(p);
            } else {
                List<List<Symbol>> combinations = generateCombinations(p.getRight(), nullable);
                for (List<Symbol> combination : combinations) {
                    if (!combination.isEmpty()) {
                        newProductions.add(new Production(p.getLeft(), combination));
                    }
                }
            }
        }

        return removeDuplicateProductions(newProductions);
    }

    private Set<NonTerminal> findNullableVariables(List<Production> productions) {
        Set<NonTerminal> nullable = new HashSet<>();
        boolean changed = true;

        while (changed) {
            changed = false;
            for (Production p : productions) {
                if (!nullable.contains(p.getLeft())) {
                    if (p.getRight().isEmpty()) {
                        nullable.add(p.getLeft());
                        changed = true;
                    }
                    else if (p.getRight().stream().allMatch(symbol ->
                            symbol instanceof NonTerminal && nullable.contains(symbol))) {
                        nullable.add(p.getLeft());
                        changed = true;
                    }
                }
            }
        }

        return nullable;
    }

    private List<List<Symbol>> generateCombinations(List<Symbol> symbols, Set<NonTerminal> nullable) {
        List<List<Symbol>> results = new ArrayList<>();
        int n = symbols.size();

        for (int mask = 0; mask < (1 << n); mask++) {
            List<Symbol> combination = new ArrayList<>();
            boolean valid = true;

            for (int i = 0; i < n; i++) {
                Symbol symbol = symbols.get(i);

                if ((mask & (1 << i)) == 0) {
                    // Include this symbol
                    combination.add(symbol);
                } else {
                    // Skip this symbol - only valid if it's a nullable NonTerminal
                    if (symbol instanceof NonTerminal && nullable.contains((NonTerminal) symbol)) {
                        // Valid to skip
                    } else {
                        valid = false;
                        break;
                    }
                }
            }

            if (valid && !combination.isEmpty()) {
                results.add(combination);
            }
        }

        return results;
    }

    private void generateCombinationsHelper(List<Symbol> symbols, Set<NonTerminal> nullable,
                                            int index, List<Symbol> current, List<List<Symbol>> results) {
        if (index == symbols.size()) {
            results.add(new ArrayList<>(current));
            return;
        }

        Symbol symbol = symbols.get(index);

        current.add(symbol);
        generateCombinationsHelper(symbols, nullable, index + 1, current, results);
        current.remove(current.size() - 1);

        if (symbol instanceof NonTerminal && nullable.contains(symbol)) {
            generateCombinationsHelper(symbols, nullable, index + 1, current, results);
        }
    }

    private List<Production> eliminateUnitProductions(List<Production> productions, Set<NonTerminal> variables) {
        List<Production> unitProductions = new ArrayList<>();
        List<Production> nonUnitProductions = new ArrayList<>();

        for (Production p : productions) {
            if (p.getRight().size() == 1 && p.getRight().get(0) instanceof NonTerminal) {
                unitProductions.add(p);
            } else {
                nonUnitProductions.add(p);
            }
        }

        Map<NonTerminal, Set<NonTerminal>> unitChains = buildUnitChains(unitProductions, variables);
        List<Production> newProductions = new ArrayList<>();

        for (NonTerminal var : variables) {
            Set<NonTerminal> reachable = unitChains.getOrDefault(var, new HashSet<>());
            reachable.add(var);

            for (NonTerminal reachableVar : reachable) {
                for (Production p : nonUnitProductions) {
                    if (p.getLeft().equals(reachableVar)) {
                        newProductions.add(new Production(var, new ArrayList<>(p.getRight())));
                    }
                }
            }
        }

        return removeDuplicateProductions(newProductions);
    }

    private Map<NonTerminal, Set<NonTerminal>> buildUnitChains(List<Production> unitProductions, Set<NonTerminal> variables) {
        Map<NonTerminal, Set<NonTerminal>> chains = new HashMap<>();

        for (NonTerminal var : variables) {
            chains.put(var, new HashSet<>());
        }

        for (Production p : unitProductions) {
            NonTerminal from = p.getLeft();
            NonTerminal to = (NonTerminal) p.getRight().get(0);
            chains.get(from).add(to);
        }

        for (NonTerminal k : variables) {
            for (NonTerminal i : variables) {
                for (NonTerminal j : variables) {
                    if (chains.get(i).contains(k) && chains.get(k).contains(j)) {
                        chains.get(i).add(j);
                    }
                }
            }
        }

        return chains;
    }

    private List<Production> convertToCNFFormat(List<Production> productions, Set<NonTerminal> variables, Set<Terminal> terminals) {
        List<Production> newProductions = new ArrayList<>();
        Map<String, NonTerminal> terminalVariables = new HashMap<>();
        Map<String, NonTerminal> intermediateVariables = new HashMap<>();  // Cache intermediate variables
        int newVarCounter = 0;

        for (Production p : productions) {
            List<Symbol> rightSide = p.getRight();

            if (rightSide.isEmpty() || (rightSide.size() == 1 && rightSide.get(0) instanceof Terminal)) {
                newProductions.add(p);
                continue;
            }

            if (rightSide.size() == 2 &&
                    rightSide.get(0) instanceof NonTerminal &&
                    rightSide.get(1) instanceof NonTerminal) {
                newProductions.add(p);
                continue;
            }

            List<Symbol> processedRight = new ArrayList<>();

            for (Symbol symbol : rightSide) {
                if (symbol instanceof Terminal) {
                    Terminal t = (Terminal) symbol;
                    NonTerminal tVar = getOrCreateTerminalVariable(t.getName(), terminalVariables, variables);
                    newProductions.add(new Production(tVar, Collections.singletonList(t)));
                    processedRight.add(tVar);
                } else {
                    processedRight.add(symbol);
                }
            }

            if (processedRight.size() > 2) {
                newProductions.addAll(breakLongProduction(p.getLeft(), processedRight, variables, intermediateVariables));
            } else {
                newProductions.add(new Production(p.getLeft(), processedRight));
            }
        }

        return removeDuplicateProductions(newProductions);
    }

    private NonTerminal getOrCreateTerminalVariable(String terminalName,
                                                    Map<String, NonTerminal> terminalVariables,
                                                    Set<NonTerminal> variables) {
        NonTerminal existing = terminalVariables.get(terminalName);
        if (existing != null) return existing;

        String newVarName = "T_" + terminalName;
        int suffix = 0;
        while (variableExists(variables, newVarName)) {
            newVarName = "T_" + terminalName + "_" + (suffix++);
        }
        NonTerminal newVar = new NonTerminal(newVarName);
        terminalVariables.put(terminalName, newVar);
        variables.add(newVar);
        return newVar;
    }

    private List<Production> breakLongProduction(NonTerminal left, List<Symbol> rightSide,
                                                 Set<NonTerminal> variables,
                                                 Map<String, NonTerminal> intermediateVariables) {
        List<Production> result = new ArrayList<>();
        if (rightSide.size() <= 2) {
            result.add(new Production(left, rightSide));
            return result;
        }

        // Build from right to left to maintain correct associativity
        // For A -> B C D E, we want: A -> B X1, X1 -> C X2, X2 -> D E

        List<NonTerminal> intermediates = new ArrayList<>();

        // Create intermediate variables for positions 1 to n-2
        for (int i = 1; i < rightSide.size() - 1; i++) {
            // Create unique key based on the remaining suffix
            String key = rightSide.subList(i, rightSide.size()).stream()
                    .map(Symbol::getName)
                    .collect(Collectors.joining("_"));

            NonTerminal newVar = intermediateVariables.get(key);
            if (newVar == null) {
                String varName = "X_" + intermediateVariables.size();
                while (variableExists(variables, varName)) {
                    varName = "X_" + (intermediateVariables.size() + variables.size());
                }
                newVar = new NonTerminal(varName);
                variables.add(newVar);
                intermediateVariables.put(key, newVar);
            }
            intermediates.add(newVar);
        }

        // First production: left -> first symbol, first intermediate
        result.add(new Production(left, Arrays.asList(rightSide.get(0), intermediates.get(0))));

        // Middle productions: intermediate[i] -> symbol[i+1], intermediate[i+1]
        for (int i = 0; i < intermediates.size() - 1; i++) {
            result.add(new Production(intermediates.get(i),
                    Arrays.asList(rightSide.get(i + 1), intermediates.get(i + 1))));
        }

        // Last production: last intermediate -> last two symbols
        result.add(new Production(intermediates.get(intermediates.size() - 1),
                Arrays.asList(rightSide.get(rightSide.size() - 2), rightSide.get(rightSide.size() - 1))));

        return result;
    }

    private String generateIntermediateKey(List<Symbol> rightSide, int position) {
        return rightSide.get(position).getName() + "_to_" + rightSide.get(rightSide.size() - 1).getName();
    }

    private String generateUniqueVariableName(Set<NonTerminal> variables, String base, int counter) {
        String name = base + "_" + counter;
        int suffix = counter;
        while (variableExists(variables, name)) {
            name = base + "_" + (suffix++);
        }
        return name;
    }

    private List<Production> removeDuplicateProductions(List<Production> productions) {
        Set<String> seen = new HashSet<>();
        List<Production> unique = new ArrayList<>();

        for (Production p : productions) {
            String key = p.getLeft().getName() + "->" +
                    p.getRight().stream().map(Symbol::getName).collect(Collectors.joining(""));

            if (!seen.contains(key)) {
                seen.add(key);
                unique.add(p);
            }
        }

        return unique;
    }

    @Override
    public ParseResult parse(String inputText) {
        List<ValidationMessage> messages = new ArrayList<>();
        try {
            CFG parsedCFG = parseFromString(inputText);

            this.variables = parsedCFG.getVariables();
            this.terminals = parsedCFG.getTerminals();
            this.productions = parsedCFG.getProductions();
            this.startSymbol = parsedCFG.getStartSymbol();
            initializeMaps();

            this.cachedCNF = this.toChomskyNormalForm();
            this.grammarStringCache = grammarToString();

            return new ParseResult(true, messages, this);
        } catch (Exception e) {
            messages.add(new ValidationMessage(e.getMessage(), 0, ValidationMessage.ValidationMessageType.ERROR));
            return new ParseResult(false, messages, null);
        }
    }

    @Override
    public ExecutionResult execute(String inputText) {
        List<ValidationMessage> messages = new ArrayList<>();

        try {
            if (startSymbol == null) {
                messages.add(new ValidationMessage("No start symbol defined", 0, ValidationMessage.ValidationMessageType.ERROR));
                return new ExecutionResult(false, messages, "ERROR: No start symbol defined");
            }

            if (productions.isEmpty()) {
                messages.add(new ValidationMessage("No productions defined", 0, ValidationMessage.ValidationMessageType.ERROR));
                return new ExecutionResult(false, messages, "ERROR: No productions defined");
            }

            if (inputText == null || inputText.isEmpty()) {
                Set<NonTerminal> nullable = findNullableVariables(this.productions);
                boolean accepted = nullable.contains(this.startSymbol);
                return new ExecutionResult(accepted, messages, "");
            }

            Set<String> validTerminals = cachedCNF.getTerminalNames();
            for (char c : inputText.toCharArray()) {
                String charStr = String.valueOf(c);
                if (!validTerminals.contains(charStr)) {
                    messages.add(new ValidationMessage("Invalid character in input: " + charStr, 0, ValidationMessage.ValidationMessageType.ERROR));
                    return new ExecutionResult(false, messages, "ERROR: Invalid character '" + charStr + "'");
                }
            }

            boolean accepted = cachedCNF.cykParse(inputText);
            return new ExecutionResult(accepted, messages, "");

        } catch (Exception e) {
            messages.add(new ValidationMessage("Execution error: " + e.getMessage(), 0, ValidationMessage.ValidationMessageType.ERROR));
            return new ExecutionResult(false, messages, "ERROR: " + e.getMessage());
        }
    }

    private String grammarToString() {
        StringBuilder sb = new StringBuilder();

        sb.append("V=").append(variables.stream()
                .map(NonTerminal::getName)
                .sorted()
                .collect(Collectors.joining(",")));

        sb.append("|T=").append(terminals.stream()
                .map(Terminal::getName)
                .sorted()
                .collect(Collectors.joining(",")));

        sb.append("|S=").append(startSymbol.getName()).append("|");

        productions.stream()
                .sorted((p1, p2) -> {
                    int leftCmp = p1.getLeft().getName().compareTo(p2.getLeft().getName());
                    if (leftCmp != 0) return leftCmp;
                    String r1 = p1.getRight().stream().map(Symbol::getName).collect(Collectors.joining());
                    String r2 = p2.getRight().stream().map(Symbol::getName).collect(Collectors.joining());
                    return r1.compareTo(r2);
                })
                .forEach(p -> sb.append(p.getLeft().getName()).append("->")
                        .append(p.getRight().stream()
                                .map(Symbol::getName)
                                .collect(Collectors.joining())).append("|"));

        return sb.toString();
    }

    private Set<String> getTerminalNames() {
        if (terminalNames == null) {
            terminalNames = terminals.stream().map(Terminal::getName).collect(Collectors.toSet());
        }
        return terminalNames;
    }

    private boolean cykParse(String input) {
        int n = input.length();
        if (n == 0) {
            return false;
        }

        int numNT = numNonTerminals;
        int arraySize = numNT * numNT;

        BitSet[][] table = new BitSet[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                table[i][j] = new BitSet(numNT);
            }
        }

        // Phase 1: Single characters
        for (int i = 0; i < n; i++) {
            String charStr = String.valueOf(input.charAt(i));
            int[] results = terminalProductionResults.get(charStr);
            if (results != null) {
                BitSet cell = table[i][0];
                for (int resultId : results) {
                    cell.set(resultId);
                }
            }
        }

        // Phase 2: Longer substrings
        for (int len = 2; len <= n; len++) {
            for (int i = 0; i <= n - len; i++) {
                BitSet targetCell = table[i][len - 1];

                for (int k = 1; k < len; k++) {
                    BitSet leftCell = table[i][k - 1];
                    BitSet rightCell = table[i + k][len - k - 1];

                    if (leftCell.isEmpty() || rightCell.isEmpty()) {
                        continue;
                    }

                    for (int leftId = leftCell.nextSetBit(0); leftId >= 0; leftId = leftCell.nextSetBit(leftId + 1)) {
                        for (int rightId = rightCell.nextSetBit(0); rightId >= 0; rightId = rightCell.nextSetBit(rightId + 1)) {
                            int key = leftId * numNT + rightId;
                            if (key < arraySize && binaryProductionResults[key] != null) {
                                int[] results = binaryProductionResults[key];
                                for (int resultId : results) {
                                    targetCell.set(resultId);
                                }
                            }
                        }
                    }
                }
            }
        }

        return startSymbolId >= 0 && table[0][n - 1].get(startSymbolId);
    }

    @Override
    public List<ValidationMessage> validate() {
        List<ValidationMessage> messages = new ArrayList<>();

        if (variables.isEmpty()) {
            messages.add(new ValidationMessage("No variables defined in the grammar", 0, ValidationMessage.ValidationMessageType.ERROR));
        }

        if (terminals.isEmpty()) {
            messages.add(new ValidationMessage("No terminals defined in the grammar", 0, ValidationMessage.ValidationMessageType.ERROR));
        }

        if (startSymbol == null) {
            messages.add(new ValidationMessage("No start symbol defined in the grammar", 0, ValidationMessage.ValidationMessageType.ERROR));
        }

        if (startSymbol != null && !variables.contains(startSymbol)) {
            messages.add(new ValidationMessage("Start symbol " + startSymbol.getName() + " is not in the set of variables", 0, ValidationMessage.ValidationMessageType.ERROR));
        }

        Set<String> varNames = variables.stream().map(NonTerminal::getName).collect(Collectors.toSet());
        Set<String> termNames = terminals.stream().map(Terminal::getName).collect(Collectors.toSet());

        for (Production p : productions) {
            if (!varNames.contains(p.getLeft().getName())) {
                messages.add(new ValidationMessage("Production uses undefined variable: " + p.getLeft().getName(), 0, ValidationMessage.ValidationMessageType.ERROR));
            }

            for (Symbol symbol : p.getRight()) {
                if (symbol instanceof NonTerminal && !varNames.contains(symbol.getName())) {
                    messages.add(new ValidationMessage("Production uses undefined variable: " + symbol.getName(), 0, ValidationMessage.ValidationMessageType.ERROR));
                } else if (symbol instanceof Terminal && !termNames.contains(symbol.getName()) && !symbol.getName().equals("eps")) {
                    messages.add(new ValidationMessage("Production uses undefined terminal: " + symbol.getName(), 0, ValidationMessage.ValidationMessageType.WARNING));
                }
            }
        }

        return messages;
    }

    public ValidationMessage validateRulesCount(int maxRules) {
        int count = productions.size();
        if (count > maxRules) {
            return new ValidationMessage(
                    String.format("CFG has %d production rules, exceeds maximum of %d", count, maxRules),
                    0,
                    ValidationMessage.ValidationMessageType.ERROR
            );
        }
        return null;
    }

    @Override
    public String toDotCode(String inputText) {
        StringBuilder dot = new StringBuilder();
        dot.append("digraph CFG {\n");
        dot.append("    rankdir=TB;\n");
        dot.append("    node [shape=box, style=rounded];\n");
        dot.append("    edge [arrowhead=vee];\n\n");

        if (startSymbol != null) {
            dot.append("    start [label=\"Start\", shape=circle, style=filled, fillcolor=lightgreen];\n");
            dot.append("    \"").append(startSymbol.getName()).append("\" [style=filled, fillcolor=lightblue];\n");
            dot.append("    start -> \"").append(startSymbol.getName()).append("\";\n\n");
        }

        int prodId = 0;
        for (Production production : productions) {
            String prodNode = "prod" + prodId++;
            String rightSide = production.getRight().stream()
                    .map(Symbol::getName)
                    .collect(Collectors.joining(" "));

            if (rightSide.isEmpty()) {
                rightSide = "ε";
            }

            dot.append("    ").append(prodNode).append(" [label=\"").append(rightSide).append("\", shape=ellipse, style=filled, fillcolor=lightyellow];\n");
            dot.append("    \"").append(production.getLeft().getName()).append("\" -> ").append(prodNode).append(";\n");

            for (Symbol symbol : production.getRight()) {
                if (symbol instanceof NonTerminal) {
                    dot.append("    ").append(prodNode).append(" -> \"").append(symbol.getName()).append("\" [style=dashed];\n");
                }
            }
        }

        dot.append("}\n");
        return dot.toString();
    }

    public void addProduction(Production p) {
        if (!variables.stream().anyMatch(v -> v.getName().equals(p.getLeft().getName()))) {
            throw new IllegalArgumentException("Left-hand side not in variables: " + p.getLeft().getName());
        }
        productions.add(p);
        productionsByLeft.computeIfAbsent(p.getLeft(), k -> new ArrayList<>()).add(p);
        this.grammarStringCache = null;  // Invalidate cache
        this.cachedCNF = null;
    }

    public void removeProduction(Production p) {
        productions.remove(p);
        List<Production> list = productionsByLeft.get(p.getLeft());
        if (list != null) {
            list.remove(p);
        }
        cachedCNF = null;
    }

    public List<Production> getProductionsFor(NonTerminal v) {
        return productionsByLeft.getOrDefault(v, new ArrayList<>());
    }

    public boolean validateGrammar() {
        if (variables.isEmpty()) {
            System.err.println("Error: No variables defined in the grammar.");
            return false;
        }

        if (terminals.isEmpty()) {
            System.err.println("Error: No terminals defined in the grammar.");
            return false;
        }

        if (startSymbol == null) {
            System.err.println("Error: No start symbol defined in the grammar.");
            return false;
        }

        if (!variables.contains(startSymbol)) {
            System.err.println("Error: Start symbol " + startSymbol.getName() +
                    " is not in the set of variables.");
            return false;
        }

        Set<String> varNames = variables.stream().map(NonTerminal::getName).collect(Collectors.toSet());
        Set<String> termNames = terminals.stream().map(Terminal::getName).collect(Collectors.toSet());

        for (Production p : productions) {
            if (!varNames.contains(p.getLeft().getName())) {
                System.err.println("Error: Production uses undefined variable: " +
                        p.getLeft().getName());
                return false;
            }

            for (Symbol symbol : p.getRight()) {
                if (symbol instanceof NonTerminal && !varNames.contains(symbol.getName())) {
                    System.err.println("Error: Production uses undefined variable: " +
                            symbol.getName());
                    return false;
                } else if (symbol instanceof Terminal &&
                        !termNames.contains(symbol.getName()) && !symbol.getName().equals("eps")) {
                    System.err.println("Error: Production uses undefined terminal: " +
                            symbol.getName());
                    return false;
                }
            }
        }

        Set<NonTerminal> variablesWithProductions = productions.stream()
                .map(Production::getLeft)
                .collect(Collectors.toSet());

        Set<NonTerminal> unusedVariables = new HashSet<>(variables);
        unusedVariables.removeAll(variablesWithProductions);

        if (!unusedVariables.isEmpty()) {
            System.err.println("Warning: The following variables have no productions: " +
                    unusedVariables.stream()
                            .map(NonTerminal::getName)
                            .collect(Collectors.joining(", ")));
        }

        Set<NonTerminal> reachable = new HashSet<>();
        reachable.add(startSymbol);

        boolean added;
        do {
            added = false;
            for (Production p : productions) {
                if (reachable.contains(p.getLeft())) {
                    for (Symbol s : p.getRight()) {
                        if (s instanceof NonTerminal && !reachable.contains(s)) {
                            reachable.add((NonTerminal) s);
                            added = true;
                        }
                    }
                }
            }
        } while (added);

        Set<NonTerminal> unreachableVariables = new HashSet<>(variables);
        unreachableVariables.removeAll(reachable);

        if (!unreachableVariables.isEmpty()) {
            System.err.println("Warning: The following variables are unreachable from the start symbol: " +
                    unreachableVariables.stream()
                            .map(NonTerminal::getName)
                            .collect(Collectors.joining(", ")));
        }

        return true;
    }

    private static String normalizeWhitespace(String input) {
        return input.replace('\u00A0', ' ')
                .replaceAll("\\s+", " ")
                .trim();
    }

    public void prettyPrint() {
        System.out.println("Variables = " + variables.stream()
                .map(NonTerminal::getName)
                .collect(Collectors.joining(" ")));

        System.out.println("Terminals = " + terminals.stream()
                .map(Terminal::getName)
                .collect(Collectors.joining(" ")));

        System.out.println("Start = " + startSymbol.getName());
        System.out.println();

        Map<NonTerminal, List<Production>> productionMap = productions.stream()
                .collect(Collectors.groupingBy(Production::getLeft));

        for (NonTerminal variable : variables) {
            List<Production> prods = productionMap.getOrDefault(variable, new ArrayList<>());
            if (!prods.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                sb.append(variable.getName()).append(" -> ");

                for (int i = 0; i < prods.size(); i++) {
                    Production p = prods.get(i);
                    if (i > 0) {
                        sb.append(" | ");
                    }

                    String rightSide = p.getRight().stream()
                            .map(Symbol::getName)
                            .collect(Collectors.joining(" "));

                    sb.append(rightSide.isEmpty() ? "eps" : rightSide);
                }

                System.out.println(sb);
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("Variables = ").append(variables.stream()
                        .map(NonTerminal::getName)
                        .collect(Collectors.joining(" ")))
                .append("\n");

        sb.append("Terminals = ").append(terminals.stream()
                        .map(Terminal::getName)
                        .collect(Collectors.joining(" ")))
                .append("\n");

        sb.append("Start = ").append(startSymbol != null ? startSymbol.getName() : "undefined")
                .append("\n\n");

        for (Production p : productions) {
            sb.append(p.getLeft().getName()).append(" -> ");

            String rightSide = p.getRight().stream()
                    .map(Symbol::getName)
                    .collect(Collectors.joining(" "));

            sb.append(rightSide.isEmpty() ? "_" : rightSide);
            sb.append("\n");
        }

        return sb.toString();
    }

    @Override
    public String getDefaultTemplate() {
        return "Variables = S A B\n" +
                "Terminals = a b\n" +
                "Start = S\n" +
                "\n" +
                "S -> A B\n" +
                "A -> a\n" +
                "B -> b\n";
    }
}