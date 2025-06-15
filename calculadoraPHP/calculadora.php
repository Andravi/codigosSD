<?php
// Calculadora remota via POST

// Função para validar e converter valores
function getPostValue($key) {
    return isset($_POST[$key]) ? floatval($_POST[$key]) : null;
}

$oper1 = getPostValue('oper1');
$oper2 = getPostValue('oper2');
$operacao = isset($_POST['operacao']) ? intval($_POST['operacao']) : null;

$resultado = null;
$erro = null;

if ($oper1 === null || $oper2 === null || $operacao === null) {
    $erro = "Parâmetros inválidos. Envie 'oper1', 'oper2' e 'operacao' via POST.";
} else {
    switch ($operacao) {
        case 1: // Soma
            $resultado = $oper1 + $oper2;
            break;
        case 2: // Subtração
            $resultado = $oper1 - $oper2;
            break;
        case 3: // Multiplicação
            $resultado = $oper1 * $oper2;
            break;
        case 4: // Divisão
            if ($oper2 == 0) {
                $erro = "Erro: divisão por zero.";
            } else {
                $resultado = $oper1 / $oper2;
            }
            break;
        default:
            $erro = "Operação inválida. Use 1 (soma), 2 (subtração), 3 (multiplicação) ou 4 (divisão).";
    }
}

// Retorno em JSON
header('Content-Type: application/json');

if ($erro) {
    echo json_encode(["erro" => $erro]);
} else {
    echo json_encode([
        "oper1" => $oper1,
        "oper2" => $oper2,
        "operacao" => $operacao,
        "resultado" => $resultado
    ]);
}
