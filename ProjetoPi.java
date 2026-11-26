import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class ProjetoPi {

    // =====================================================================
    // DADOS DO SISTEMA (variáveis da classe, usadas por todos os métodos)
    // =====================================================================

    public static Scanner ent;

    // Cartões válidos no sistema
    public static ArrayList<Short> cartoesNoSistema = new ArrayList<>();

    // Catálogo normal (listas paralelas: o mesmo índice é o mesmo produto)
    public static ArrayList<Integer> codigosNormal = new ArrayList<>();
    public static ArrayList<String> nomesNormal = new ArrayList<>();
    public static ArrayList<Double> precosNormal = new ArrayList<>();
    public static ArrayList<Integer> estoqueNormal = new ArrayList<>();

    // Catálogo exclusivo VIP
    public static ArrayList<Integer> codigosVip = new ArrayList<>();
    public static ArrayList<String> nomesVip = new ArrayList<>();
    public static ArrayList<Double> precosVip = new ArrayList<>();
    public static ArrayList<Integer> estoqueVip = new ArrayList<>();

    // Carrinho do cliente atual
    public static ArrayList<String> carrinhoNomes = new ArrayList<>();
    public static ArrayList<Double> carrinhoPrecos = new ArrayList<>();
    // Guarda de onde veio cada item (para devolver ao estoque se for removido do carrinho)
    public static ArrayList<Integer> carrinhoCodigos = new ArrayList<>();
    public static ArrayList<Boolean> carrinhoVip = new ArrayList<>();

    // Modelo por cor
    public static String[] coresDisponiveis = {"Preto/Branco", "Branco Total", "Vermelho/Preto", "Azul/Cinza", "Custom Especial"};

    // Acumuladores para o relatório final do dia
    public static double faturamentoTotalDia = 0.0;
    public static double boletosAguardandoDia = 0.0; // Boleto só vira faturamento quando for pago
    public static int quantidadeVendasDia = 0;
    public static double impostoVendasDia = 0.0;      // imposto sobre a venda já cobrado no dia (boleto só conta quando pago)
    public static double taxasVendasDia = 0.0;        // taxas (cartão, parcelamento, gateway) cobradas nas vendas pagas do dia
    public static int itensVendidosDia = 0;           // pares vendidos nas vendas pagas do dia
    public static int vendasPagasDia = 0;             // vendas pagas do dia (boleto não entra)

    // Acumulado desde o início do sistema (NÃO zera no fim do expediente)
    public static int acumVendas = 0;
    public static double acumFaturamento = 0.0;
    public static double acumImpostoVendas = 0.0;
    public static double acumTaxas = 0.0;
    public static int acumItens = 0;
    public static int acumVendasPagas = 0;
    // CBS e IBS não ficam guardados: são calculados uma única vez sobre o faturamento acumulado

    // Alíquotas e descontos (podem ser alterados no painel administrativo)
    public static double aliquotaImposto = 0.04;      // 4% de imposto
    public static double aliquotaTaxa = 0.06;         // 6% de taxa
    public static double aliquotaVip = 0.05;          // 5% de desconto VIP
    public static double aliquotaPix = 0.05;          // 5% de desconto no PIX
    // 2027: CBS = alíquota de referência (TCU calculou 11,3%, pendente de resolução do Senado até 15/12/2026) - 0,1 p.p. que vai para o IBS
    public static double aliquotaCbsBruta = 0.112;    // 11,2% de CBS bruta (2027)
    public static double aliquotaCbsEfetiva = 0.0368; // 3,68% de CBS efetiva (premissa do grupo, sem fonte oficial: revisar)
    public static double aliquotaIbs = 0.001;         // 0,1% de IBS (2027)

    // Custos da tabela da proposta (valores iniciais de simulação), usados no cálculo do lucro
    public static double custoCompraUnidade = 500.00;    // custo de compra de cada par
    public static double freteEntradaUnidade = 20.00;    // frete de entrada de cada par
    public static double embalagemPorVenda = 5.00;       // embalagem de cada venda
    public static double subsidioFretePorVenda = 10.00;  // subsídio de frete de cada venda

    // Senha do painel administrativo: guardamos o hash (String.hashCode) e não a senha em texto.
    // Para trocar a senha: rode System.out.println("nova senha".hashCode()) e cole o número abaixo.
    public static int senhaAdminHash = -969161597;

    // Pastas onde os dados são salvos (principal e backup na pasta do usuário)
    public static String pastaPrincipal = System.getProperty("nikestore.principal", "C:" + File.separator + "NikeStore" + File.separator + "dados");
    public static String pastaBackup = System.getProperty("nikestore.backup", System.getProperty("user.home") + File.separator + "NikeStoreBackup");
    public static String[] pastasDestino = {pastaPrincipal, pastaBackup};

    // =====================================================================
    // MAIN
    // =====================================================================

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            ent = scanner;

            carregarDadosIniciais();
            carregarDados();

            boolean sistemaAtivo = true;
            while (sistemaAtivo) {
                limparCarrinho();

                System.out.println("\n=========================================");
                System.out.println("    LOJA DE TÊNIS CUSTOMIZADOS          ");
                System.out.println("=========================================");

                String resposta = lerRespostaCartao();
                boolean possuiCartao = false;
                boolean escolhaFeita = false;

                while (!escolhaFeita) {
                    if (ehAdmin(resposta)) {
                        boolean voltou = painelAdmin(); // true se digitou 0 na senha
                        if (voltou) {
                            resposta = lerRespostaCartao(); // volta a escolher convencional, VIP ou ADM
                        } else {
                            possuiCartao = lerSimNao("Possui cartão exclusivo da loja? (sim [s] / não [n]): ");
                            escolhaFeita = true;
                        }
                    } else {
                        possuiCartao = ehSim(resposta);
                        escolhaFeita = true;
                    }
                }

                boolean clienteExclusivo = false;
                if (possuiCartao) {
                    clienteExclusivo = validarCartao();
                }

                fazerCompras(clienteExclusivo);
                removerItensDoCarrinho();
                finalizarPedido(clienteExclusivo);

                System.out.println("=========================================================");
                System.out.println("        Obrigado por comprar na nossa loja!              ");
                System.out.println("=========================================================");

                if (lerSimNao("\nEsta foi a última venda do dia? (sim [s] / não [n]): ")) {
                    sistemaAtivo = false;
                    encerrarExpediente();
                } else {
                    System.out.println("\n>> Reiniciando o sistema para o próximo cliente...\n");
                }

                // Roda no fim de cada atendimento: cobre vendas, mudanças do admin e encerramento do dia
                salvarDados(!sistemaAtivo);
            }
        } catch (NoSuchElementException e) {
            // Acontece se a entrada de dados terminar de repente (Ctrl+D ou arquivo de entrada incompleto)
            System.out.println("\n>> Entrada de dados encerrada. Programa finalizado.");
        }
    }

    // =====================================================================
    // DADOS INICIAIS
    // =====================================================================

    public static void carregarDadosIniciais() {
        for (short i = 1; i <= 10; i++) {
            cartoesNoSistema.add(i);
        }

        int[] codigosN = {100, 101, 102, 103, 104, 105, 106, 107, 108, 109};
        String[] nomesN = {
            "Nike Air Force 1", "Nike Air Max 1", "Nike Air Max 90",
            "Nike Air Max 270", "Nike Dunk Low", "Nike Dunk High",
            "Nike Pegasus 41", "Nike Vomero 5", "Nike Air Huarache",
            "Nike React Infinity Run"
        };
        double[] precosN = {759.99, 1044.99, 769.99, 899.99, 854.99, 598.49, 661.67, 1234.99, 702.99, 1135.24};

        for (int i = 0; i < codigosN.length; i++) {
            codigosNormal.add(codigosN[i]);
            nomesNormal.add(nomesN[i]);
            precosNormal.add(precosN[i]);
            estoqueNormal.add(5); // 5 unidades por padrão
        }

        int[] codigosV = {200, 201, 202, 203, 204, 205, 206, 207, 208, 209};
        String[] nomesV = {
            "Nike Air Max 95", "Nike Air Max 97", "Nike Air Max Plus (TN)",
            "Nike Air Jordan 1 Mid", "Nike Air Jordan 4", "Nike Air Jordan 11",
            "Nike Blazer Mid", "Nike Cortez", "Nike P-6000", "Nike LeBron 22"
        };
        double[] precosV = {1399.99, 1424.99, 1329.99, 1499.99, 1709.99, 1709.99, 885.15, 645.99, 854.99, 1519.99};

        for (int i = 0; i < codigosV.length; i++) {
            codigosVip.add(codigosV[i]);
            nomesVip.add(nomesV[i]);
            precosVip.add(precosV[i]);
            estoqueVip.add(5);
        }
    }

    // =====================================================================
    // FUNÇÕES AUXILIARES (leitura, validação e cálculo)
    // =====================================================================

    public static boolean ehSim(String r) {
        return r.equalsIgnoreCase("s") || r.equalsIgnoreCase("sim");
    }

    public static boolean ehNao(String r) {
        return r.equalsIgnoreCase("n") || r.equalsIgnoreCase("nao") || r.equalsIgnoreCase("não");
    }

    public static boolean ehAdmin(String r) {
        return r.equalsIgnoreCase("a") || r.equalsIgnoreCase("admin");
    }

    // Repete a pergunta até o usuário responder sim ou não. Retorna true para sim.
    public static boolean lerSimNao(String pergunta) {
        while (true) {
            System.out.print(pergunta);
            String resposta = ent.nextLine().trim();
            if (ehSim(resposta)) {
                return true;
            }
            if (ehNao(resposta)) {
                return false;
            }
            System.out.println(">> Resposta inválida! Digite s (sim) ou n (não).");
        }
    }

    // Pergunta inicial: sim, não ou admin
    public static String lerRespostaCartao() {
        while (true) {
            System.out.print("\nPossui cartão exclusivo da loja? (sim [s] / não [n] / admin [a]): ");
            String resposta = ent.nextLine().trim();
            if (ehSim(resposta) || ehNao(resposta) || ehAdmin(resposta)) {
                return resposta;
            }
            System.out.println(">> Resposta inválida! Digite s (sim), n (não) ou a (admin).");
        }
    }

    // Lê um número inteiro dentro de um intervalo, repetindo até ser válido
    public static int lerNumeroNoIntervalo(String pergunta, int min, int max, String msgForaDaFaixa, String msgNaoNumero) {
        int numero = min - 1;
        while (numero < min || numero > max) {
            try {
                System.out.print(pergunta);
                numero = ent.nextInt();
                ent.nextLine(); // Limpeza do buffer
                if (numero < min || numero > max) {
                    System.out.println(msgForaDaFaixa);
                }
            } catch (InputMismatchException e) {
                System.out.println(msgNaoNumero);
                ent.nextLine();
                numero = min - 1;
            }
        }
        return numero;
    }

    // Aceita ponto ou vírgula, independente da região do computador
    public static double lerDecimal() {
        return Double.parseDouble(ent.nextLine().trim().replace(',', '.'));
    }

    public static boolean precoValido(double preco) {
        return preco > 0 && preco < Double.POSITIVE_INFINITY;
    }

    // Arredonda para centavos, para a soma nunca divergir do que aparece na tela
    public static double arredondar(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }

    // Escolhe a lista certa conforme o catálogo (VIP ou convencional)
    public static ArrayList<Integer> listaCodigos(boolean vip) {
        return vip ? codigosVip : codigosNormal;
    }

    public static ArrayList<String> listaNomes(boolean vip) {
        return vip ? nomesVip : nomesNormal;
    }

    public static ArrayList<Double> listaPrecos(boolean vip) {
        return vip ? precosVip : precosNormal;
    }

    public static ArrayList<Integer> listaEstoque(boolean vip) {
        return vip ? estoqueVip : estoqueNormal;
    }

    // =====================================================================
    // ARQUIVOS: CARREGAR E SALVAR
    // =====================================================================

    // Se já existir um arquivo salvo (principal ou backup), ele substitui os dados iniciais
    public static void carregarDados() {
        boolean dadosCarregados = false;
        boolean encontrouArquivoInvalido = false;

        for (int origem = 0; origem < pastasDestino.length && !dadosCarregados; origem++) {
            File arquivoLeitura = new File(pastasDestino[origem], "catalogo.txt");
            if (!arquivoLeitura.exists()) {
                continue;
            }

            if (lerArquivoCatalogo(arquivoLeitura)) {
                dadosCarregados = true;
                System.out.println(">> Dados carregados de: " + arquivoLeitura.getPath());
            } else {
                encontrouArquivoInvalido = true;
                System.out.println(">> Aviso: o arquivo " + arquivoLeitura.getPath() + " está incompleto ou inválido. Tentando a próxima pasta...");
            }
        }

        if (!dadosCarregados) {
            if (encontrouArquivoInvalido) {
                System.out.println(">> Nenhum arquivo válido encontrado. Usando os dados iniciais.");
            } else {
                System.out.println(">> Nenhum arquivo salvo encontrado. Usando os dados iniciais.");
            }
        }
    }

    // Lê o arquivo para listas temporárias. Os dados reais só são trocados se o arquivo estiver completo e válido.
    public static boolean lerArquivoCatalogo(File arquivoLeitura) {
        ArrayList<Integer> lerCodigosN = new ArrayList<>();
        ArrayList<String> lerNomesN = new ArrayList<>();
        ArrayList<Double> lerPrecosN = new ArrayList<>();
        ArrayList<Integer> lerEstoqueN = new ArrayList<>();
        ArrayList<Integer> lerCodigosV = new ArrayList<>();
        ArrayList<String> lerNomesV = new ArrayList<>();
        ArrayList<Double> lerPrecosV = new ArrayList<>();
        ArrayList<Integer> lerEstoqueV = new ArrayList<>();
        double[] lerAliquotas = new double[7];
        lerAliquotas[6] = aliquotaIbs; // arquivo versão 1 não tem IBS: mantém o valor atual
        double[] lerDia = new double[7];
        double[] lerAcum = new double[6];
        int versao = 3;

        boolean arquivoValido = true;
        boolean leuAliquotas = false;
        boolean leuDia = false;
        boolean leuAcum = false;
        int linhasLidas = 0;

        try (Scanner leitor = new Scanner(arquivoLeitura, "UTF-8")) {
            while (leitor.hasNextLine() && arquivoValido) {
                String linha = leitor.nextLine().trim();
                if (linha.isEmpty()) {
                    continue;
                }
                linhasLidas++;
                String[] campos = linha.split(";", -1);

                if (linhasLidas == 1) {
                    // A primeira linha tem que ser o cabeçalho do arquivo
                    arquivoValido = linha.equals("NIKESTORE;1") || linha.equals("NIKESTORE;2") || linha.equals("NIKESTORE;3");
                    if (linha.equals("NIKESTORE;1")) {
                        versao = 1;
                    } else if (linha.equals("NIKESTORE;2")) {
                        versao = 2;
                    }
                } else if (campos[0].equals("N") || campos[0].equals("V")) {
                    // Produto: tipo(N/V);código;nome;preço;estoque
                    if (campos.length != 5) {
                        arquivoValido = false;
                    } else {
                        try {
                            int codigoLido = Integer.parseInt(campos[1].trim());
                            String nomeLido = campos[2].trim();
                            double precoLido = Double.parseDouble(campos[3].trim());
                            int estoqueLido = Integer.parseInt(campos[4].trim());
                            ArrayList<Integer> lerCodigosAtual = campos[0].equals("V") ? lerCodigosV : lerCodigosN;

                            if (nomeLido.isEmpty() || !precoValido(precoLido)
                                    || estoqueLido < 0 || lerCodigosAtual.indexOf(codigoLido) != -1) {
                                arquivoValido = false;
                            } else if (campos[0].equals("V")) {
                                lerCodigosV.add(codigoLido);
                                lerNomesV.add(nomeLido);
                                lerPrecosV.add(precoLido);
                                lerEstoqueV.add(estoqueLido);
                            } else {
                                lerCodigosN.add(codigoLido);
                                lerNomesN.add(nomeLido);
                                lerPrecosN.add(precoLido);
                                lerEstoqueN.add(estoqueLido);
                            }
                        } catch (NumberFormatException e) {
                            arquivoValido = false;
                        }
                    }
                } else if (campos[0].equals("ALIQ") && !leuAliquotas) {
                    // Alíquotas: imposto;taxa;vip;pix;cbsBruta;cbsEfetiva;ibs (todas entre 0 e 1; versão 1 não tem o ibs)
                    int qtdAliquotas = (versao == 1) ? 6 : 7;
                    if (campos.length != qtdAliquotas + 1) {
                        arquivoValido = false;
                    } else {
                        try {
                            for (int i = 0; i < qtdAliquotas; i++) {
                                lerAliquotas[i] = Double.parseDouble(campos[i + 1].trim());
                                if (!(lerAliquotas[i] >= 0 && lerAliquotas[i] <= 1)) {
                                    arquivoValido = false;
                                }
                            }
                            leuAliquotas = true;
                        } catch (NumberFormatException e) {
                            arquivoValido = false;
                        }
                    }
                } else if (campos[0].equals("DIA") && !leuDia) {
                    // Totais do dia: vendas;faturamento;boletos aguardando;imposto das vendas;taxas;itens;vendas pagas
                    // (versão 1 só vai até os boletos, versão 2 até o imposto das vendas)
                    int qtdCamposDia = (versao == 1) ? 4 : (versao == 2) ? 5 : 8;
                    if (campos.length != qtdCamposDia) {
                        arquivoValido = false;
                    } else {
                        try {
                            int vendasLidas = Integer.parseInt(campos[1].trim());
                            double faturamentoLido = Double.parseDouble(campos[2].trim());
                            double boletosLidos = Double.parseDouble(campos[3].trim());
                            double impostoLido = (versao == 1) ? 0.0 : Double.parseDouble(campos[4].trim());
                            double taxasLidas = (versao == 3) ? Double.parseDouble(campos[5].trim()) : 0.0;
                            int itensLidos = (versao == 3) ? Integer.parseInt(campos[6].trim()) : 0;
                            int pagasLidas = (versao == 3) ? Integer.parseInt(campos[7].trim()) : 0;
                            if (vendasLidas < 0 || !(faturamentoLido >= 0 && faturamentoLido < Double.POSITIVE_INFINITY)
                                    || !(boletosLidos >= 0 && boletosLidos < Double.POSITIVE_INFINITY)
                                    || !(impostoLido >= 0 && impostoLido < Double.POSITIVE_INFINITY)
                                    || !(taxasLidas >= 0 && taxasLidas < Double.POSITIVE_INFINITY)
                                    || itensLidos < 0 || pagasLidas < 0) {
                                arquivoValido = false;
                            } else {
                                lerDia[0] = vendasLidas;
                                lerDia[1] = faturamentoLido;
                                lerDia[2] = boletosLidos;
                                lerDia[3] = impostoLido;
                                lerDia[4] = taxasLidas;
                                lerDia[5] = itensLidos;
                                lerDia[6] = pagasLidas;
                                leuDia = true;
                            }
                        } catch (NumberFormatException e) {
                            arquivoValido = false;
                        }
                    }
                } else if (campos[0].equals("ACUM") && !leuAcum && versao >= 2) {
                    // Acumulado versão 3: vendas;faturamento;imposto das vendas;taxas;itens;vendas pagas
                    // Versão 2: vendas;faturamento;imposto das vendas (podia trazer ;cbs;ibs, que são ignorados)
                    boolean tamanhoOk = (versao == 3) ? campos.length == 7 : (campos.length == 4 || campos.length == 6);
                    if (!tamanhoOk) {
                        arquivoValido = false;
                    } else {
                        try {
                            int qtdLer = (versao == 3) ? 6 : 3;
                            boolean todosValidos = true;
                            for (int i = 0; i < qtdLer; i++) {
                                lerAcum[i] = Double.parseDouble(campos[i + 1].trim());
                                if (!(lerAcum[i] >= 0 && lerAcum[i] < Double.POSITIVE_INFINITY)) {
                                    todosValidos = false;
                                }
                            }
                            if (todosValidos) {
                                leuAcum = true;
                            } else {
                                arquivoValido = false;
                            }
                        } catch (NumberFormatException e) {
                            arquivoValido = false;
                        }
                    }
                } else {
                    arquivoValido = false;
                }
            }
        } catch (FileNotFoundException e) {
            arquivoValido = false;
        }

        if (!(arquivoValido && leuAliquotas && leuDia && (versao == 1 || leuAcum))) {
            return false;
        }

        codigosNormal = lerCodigosN;
        nomesNormal = lerNomesN;
        precosNormal = lerPrecosN;
        estoqueNormal = lerEstoqueN;
        codigosVip = lerCodigosV;
        nomesVip = lerNomesV;
        precosVip = lerPrecosV;
        estoqueVip = lerEstoqueV;
        aliquotaImposto = lerAliquotas[0];
        aliquotaTaxa = lerAliquotas[1];
        aliquotaVip = lerAliquotas[2];
        aliquotaPix = lerAliquotas[3];
        aliquotaCbsBruta = lerAliquotas[4];
        aliquotaCbsEfetiva = lerAliquotas[5];
        aliquotaIbs = lerAliquotas[6];
        quantidadeVendasDia = (int) lerDia[0];
        faturamentoTotalDia = lerDia[1];
        boletosAguardandoDia = lerDia[2];
        impostoVendasDia = lerDia[3];
        taxasVendasDia = lerDia[4];
        itensVendidosDia = (int) lerDia[5];
        vendasPagasDia = (int) lerDia[6];
        acumVendas = (int) lerAcum[0];
        acumFaturamento = lerAcum[1];
        acumImpostoVendas = lerAcum[2];
        acumTaxas = lerAcum[3];
        acumItens = (int) lerAcum[4];
        acumVendasPagas = (int) lerAcum[5];
        return true;
    }

    // Grava catálogo, estoque, alíquotas e totais do dia na pasta principal e no backup
    public static void salvarDados(boolean mostrarCaminho) {
        for (int i = 0; i < pastasDestino.length; i++) {
            File pasta = new File(pastasDestino[i]);
            pasta.mkdirs(); // cria a pasta se ela ainda não existir
            File arquivoTemporario = new File(pasta, "catalogo.tmp");
            File arquivoDefinitivo = new File(pasta, "catalogo.txt");
            boolean gravou = true;

            // Grava primeiro em um arquivo temporário: se algo falhar no meio, o arquivo bom anterior não se perde
            try (BufferedWriter escritor = new BufferedWriter(new FileWriter(arquivoTemporario, StandardCharsets.UTF_8))) {
                escritor.write("NIKESTORE;3");
                escritor.newLine();
                for (int j = 0; j < codigosNormal.size(); j++) {
                    escritor.write("N;" + codigosNormal.get(j) + ";" + nomesNormal.get(j) + ";" + precosNormal.get(j) + ";" + estoqueNormal.get(j));
                    escritor.newLine();
                }
                for (int j = 0; j < codigosVip.size(); j++) {
                    escritor.write("V;" + codigosVip.get(j) + ";" + nomesVip.get(j) + ";" + precosVip.get(j) + ";" + estoqueVip.get(j));
                    escritor.newLine();
                }
                escritor.write("ALIQ;" + aliquotaImposto + ";" + aliquotaTaxa + ";" + aliquotaVip + ";" + aliquotaPix
                        + ";" + aliquotaCbsBruta + ";" + aliquotaCbsEfetiva + ";" + aliquotaIbs);
                escritor.newLine();
                escritor.write("DIA;" + quantidadeVendasDia + ";" + faturamentoTotalDia + ";" + boletosAguardandoDia + ";" + impostoVendasDia
                        + ";" + taxasVendasDia + ";" + itensVendidosDia + ";" + vendasPagasDia);
                escritor.newLine();
                escritor.write("ACUM;" + acumVendas + ";" + acumFaturamento + ";" + acumImpostoVendas
                        + ";" + acumTaxas + ";" + acumItens + ";" + acumVendasPagas);
                escritor.newLine();
            } catch (IOException e) {
                gravou = false;
                System.out.println(">> Aviso: não foi possível salvar em " + pastasDestino[i] + " (" + e.getMessage() + ")");
            }

            // Só troca o arquivo antigo pelo novo se a gravação deu certo
            if (gravou) {
                if (arquivoDefinitivo.exists()) {
                    arquivoDefinitivo.delete();
                }
                if (arquivoTemporario.renameTo(arquivoDefinitivo)) {
                    if (mostrarCaminho) {
                        System.out.println(">> Dados salvos em: " + arquivoDefinitivo.getPath());
                    }
                } else {
                    System.out.println(">> Aviso: não foi possível finalizar o arquivo em " + pastasDestino[i]);
                }
            }
        }
    }

    // =====================================================================
    // PAINEL ADMINISTRATIVO
    // =====================================================================

    // Retorna 1 se a senha estiver certa, 2 se errou as 3 tentativas e 0 se o usuário digitou 0 para voltar
    public static int pedirSenhaAdmin() {
        int tentativas = 0;
        while (tentativas < 3) {
            System.out.print("Digite a senha do administrador (ou 0 para voltar): ");
            String senhaDigitada = ent.nextLine().trim();
            if (senhaDigitada.equals("0")) {
                System.out.println(">> Voltando para a escolha inicial...");
                return 0;
            }
            if (senhaDigitada.hashCode() == senhaAdminHash) {
                return 1;
            }
            tentativas++;
            System.out.println(">> Senha incorreta! Tentativa " + tentativas + " de 3.");
        }
        return 2;
    }

    public static void mostrarMenuAdmin() {
        System.out.println("\n=========================================");
        System.out.println("       PAINEL ADMINISTRATIVO (ADM)       ");
        System.out.println("=========================================");
        System.out.println("1 - Alterar Preço de Produto");
        System.out.println("2 - Repor/Alterar Estoque de Produto");
        System.out.println("3 - Adicionar Novo Produto ao Catálogo");
        System.out.println("4 - Remover Produto do Catálogo");
        System.out.println("5 - Alterar Alíquotas e Descontos");
        System.out.println("6 - Sair do Modo Admin e Ir para a Loja");
        System.out.print("Escolha uma opção: ");
    }

    // Retorna true se o usuário digitou 0 na senha para voltar à escolha inicial
    public static boolean painelAdmin() {
        int resultadoSenha = pedirSenhaAdmin();
        if (resultadoSenha == 0) {
            return true;
        }
        if (resultadoSenha == 2) {
            System.out.println(">> Acesso ao painel administrativo negado.\n");
            return false;
        }

        boolean emModoAdmin = true;
        while (emModoAdmin) {
            mostrarMenuAdmin();

            try {
                int opcaoAdmin = ent.nextInt();
                ent.nextLine(); // Limpeza do buffer

                if (opcaoAdmin == 1) {
                    alterarPreco();
                } else if (opcaoAdmin == 2) {
                    alterarEstoque();
                } else if (opcaoAdmin == 3) {
                    adicionarProduto();
                } else if (opcaoAdmin == 4) {
                    removerProduto();
                } else if (opcaoAdmin == 5) {
                    alterarAliquotas();
                } else if (opcaoAdmin == 6) {
                    emModoAdmin = false;
                    System.out.println("\nSaindo do modo ADM...\n");
                } else {
                    System.out.println(">> Opção inválida!");
                }
            } catch (InputMismatchException e) {
                System.out.println(">> Entrada inválida no painel ADM!\n");
                ent.nextLine(); // Limpa a entrada inválida
            } catch (NumberFormatException e) {
                // A linha já foi lida inteira, então não precisa limpar o buffer
                System.out.println(">> Valor numérico inválido! Use apenas números (ex.: 759.99 ou 759,99).\n");
            }
        }
        return false; // saiu do painel normalmente (opção 6)
    }

    public static void alterarPreco() {
        System.out.print("Qual catálogo deseja alterar? (1 - Convencional | 2 - VIP): ");
        int opcaoCatalogo = ent.nextInt();
        System.out.print("Digite o código do produto: ");
        int codigoAlvo = ent.nextInt();
        ent.nextLine(); // Limpeza do buffer
        System.out.print("Digite o novo preço: R$ ");
        double precoNovo = lerDecimal();

        if (opcaoCatalogo != 1 && opcaoCatalogo != 2) {
            System.out.println(">> Catálogo inválido! Escolha 1 ou 2.");
        } else if (!precoValido(precoNovo)) {
            System.out.println(">> Preço inválido! O preço deve ser maior que zero.");
        } else {
            boolean vip = (opcaoCatalogo == 2);
            int indice = listaCodigos(vip).indexOf(codigoAlvo);
            if (indice != -1) {
                listaPrecos(vip).set(indice, precoNovo);
                System.out.println(">> Preço atualizado com sucesso!");
            } else {
                System.out.println(">> Produto não encontrado!");
            }
        }
    }

    public static void alterarEstoque() {
        System.out.print("Qual catálogo deseja alterar? (1 - Convencional | 2 - VIP): ");
        int opcaoCatalogo = ent.nextInt();
        System.out.print("Digite o código do produto: ");
        int codigoAlvo = ent.nextInt();
        System.out.print("Digite a nova quantidade em estoque: ");
        int estoqueNovo = ent.nextInt();
        ent.nextLine(); // Limpeza do buffer

        if (opcaoCatalogo != 1 && opcaoCatalogo != 2) {
            System.out.println(">> Catálogo inválido! Escolha 1 ou 2.");
        } else if (estoqueNovo < 0) {
            System.out.println(">> Estoque inválido! A quantidade não pode ser negativa.");
        } else {
            boolean vip = (opcaoCatalogo == 2);
            int indice = listaCodigos(vip).indexOf(codigoAlvo);
            if (indice != -1) {
                listaEstoque(vip).set(indice, estoqueNovo);
                System.out.println(">> Estoque atualizado com sucesso!");
            } else {
                System.out.println(">> Produto não encontrado!");
            }
        }
    }

    public static void adicionarProduto() {
        System.out.print("Em qual catálogo deseja adicionar? (1 - Convencional | 2 - VIP): ");
        int opcaoCatalogo = ent.nextInt();
        System.out.print("Digite o código do novo produto: ");
        int codigoNovo = ent.nextInt();
        ent.nextLine(); // Limpeza do buffer
        System.out.print("Digite o nome do produto: ");
        String nomeNovo = ent.nextLine().trim();
        System.out.print("Digite o preço: R$ ");
        double precoNovo = lerDecimal();
        System.out.print("Digite a quantidade inicial de estoque: ");
        int estoqueNovo = ent.nextInt();
        ent.nextLine(); // Limpeza do buffer

        boolean vip = (opcaoCatalogo == 2);
        boolean codigoJaExiste = listaCodigos(vip).indexOf(codigoNovo) != -1;

        if (opcaoCatalogo != 1 && opcaoCatalogo != 2) {
            System.out.println(">> Catálogo inválido! Escolha 1 ou 2.");
        } else if (nomeNovo.isEmpty()) {
            System.out.println(">> O nome do produto não pode ficar em branco!");
        } else if (nomeNovo.indexOf(';') != -1) {
            System.out.println(">> O nome do produto não pode ter ponto e vírgula (;)!");
        } else if (codigoJaExiste) {
            System.out.println(">> Já existe um produto com esse código neste catálogo!");
        } else if (!precoValido(precoNovo) || estoqueNovo < 0) {
            System.out.println(">> Preço deve ser maior que zero e estoque não pode ser negativo!");
        } else {
            listaCodigos(vip).add(codigoNovo);
            listaNomes(vip).add(nomeNovo);
            listaPrecos(vip).add(precoNovo);
            listaEstoque(vip).add(estoqueNovo);
            System.out.println(">> Novo produto cadastrado com sucesso!");
        }
    }

    public static void removerProduto() {
        System.out.print("Qual catálogo deseja alterar? (1 - Convencional | 2 - VIP): ");
        int opcaoCatalogo = ent.nextInt();
        System.out.print("Digite o código do produto a remover: ");
        int codigoAlvo = ent.nextInt();
        ent.nextLine(); // Limpeza do buffer

        if (opcaoCatalogo != 1 && opcaoCatalogo != 2) {
            System.out.println(">> Catálogo inválido! Escolha 1 ou 2.");
        } else {
            boolean vip = (opcaoCatalogo == 2);
            int indice = listaCodigos(vip).indexOf(codigoAlvo);
            if (indice != -1) {
                // Remove nas 4 listas paralelas para mantê-las sincronizadas
                listaCodigos(vip).remove(indice);
                listaNomes(vip).remove(indice);
                listaPrecos(vip).remove(indice);
                listaEstoque(vip).remove(indice);
                System.out.println(">> Produto removido com sucesso!");
            } else {
                System.out.println(">> Produto não encontrado!");
            }
        }
    }

    public static void alterarAliquotas() {
        System.out.println("\n--- ALÍQUOTAS E DESCONTOS ATUAIS ---");
        System.out.printf("1 - Imposto sobre a venda:          %.2f%%%n", aliquotaImposto * 100);
        System.out.printf("2 - Taxa sobre a venda:             %.2f%%%n", aliquotaTaxa * 100);
        System.out.printf("3 - Desconto VIP:                   %.2f%%%n", aliquotaVip * 100);
        System.out.printf("4 - Desconto no PIX:                %.2f%%%n", aliquotaPix * 100);
        System.out.printf("5 - CBS bruta (nota de venda):      %.2f%%%n", aliquotaCbsBruta * 100);
        System.out.printf("6 - CBS efetiva (impacto no caixa): %.2f%%%n", aliquotaCbsEfetiva * 100);
        System.out.printf("7 - IBS:                            %.2f%%%n", aliquotaIbs * 100);
        System.out.print("Qual deseja alterar? (1 a 7): ");
        int opcaoAliquota = ent.nextInt();
        ent.nextLine(); // Limpeza do buffer
        System.out.print("Digite o novo valor em porcentagem (0 a 100): ");
        double novaAliquota = lerDecimal() / 100;

        if (opcaoAliquota < 1 || opcaoAliquota > 7) {
            System.out.println(">> Opção inválida! Escolha de 1 a 7.");
        } else if (!(novaAliquota >= 0 && novaAliquota <= 1)) {
            System.out.println(">> Valor inválido! Digite um número entre 0 e 100.");
        } else {
            if (opcaoAliquota == 1) {
                aliquotaImposto = novaAliquota;
            } else if (opcaoAliquota == 2) {
                aliquotaTaxa = novaAliquota;
            } else if (opcaoAliquota == 3) {
                aliquotaVip = novaAliquota;
            } else if (opcaoAliquota == 4) {
                aliquotaPix = novaAliquota;
            } else if (opcaoAliquota == 5) {
                aliquotaCbsBruta = novaAliquota;
            } else if (opcaoAliquota == 6) {
                aliquotaCbsEfetiva = novaAliquota;
            } else {
                aliquotaIbs = novaAliquota;
            }
            System.out.println(">> Alíquota atualizada com sucesso!");
        }
    }

    // =====================================================================
    // CARTÃO VIP
    // =====================================================================

    // Retorna true se o cartão for válido, false se o cliente digitar 0 para seguir sem cartão
    public static boolean validarCartao() {
        while (true) {
            try {
                System.out.print("Digite o número do seu cartão (ou 0 para continuar sem cartão): ");
                long numeroCartao = ent.nextLong();
                ent.nextLine(); // Limpeza do buffer

                if (numeroCartao == 0) {
                    System.out.println("\n>> Continuando sem cartão exclusivo.");
                    return false;
                }

                for (short cartao : cartoesNoSistema) {
                    if (numeroCartao == cartao) {
                        System.out.println("\n>> Cartão Exclusivo Validado com Sucesso! <<");
                        return true;
                    }
                }
                System.out.println(">> Cartão inválido! Tente novamente.\n");
            } catch (InputMismatchException e) {
                System.out.println(">> Entrada inválida! Por favor, digite apenas números.\n");
                ent.nextLine(); // Limpa a entrada inválida
            }
        }
    }

    // =====================================================================
    // COMPRA: CATÁLOGO, PRODUTO, COR, TAMANHO E CARRINHO
    // =====================================================================

    public static void limparCarrinho() {
        carrinhoNomes.clear();
        carrinhoPrecos.clear();
        carrinhoCodigos.clear();
        carrinhoVip.clear();
    }

    // Repete a compra de itens até o cliente dizer que não quer continuar
    public static void fazerCompras(boolean clienteExclusivo) {
        boolean continuar;
        do {
            boolean verCatalogoVip = escolherCatalogo(clienteExclusivo);
            mostrarCatalogo(verCatalogoVip);
            escolherProduto(verCatalogoVip, clienteExclusivo);
            continuar = lerSimNao("\nDeseja continuar a comprar? (sim [s] / não [n]): ");
        } while (continuar);
    }

    // Cliente com cartão escolhe a loja; os demais vão direto para a convencional
    public static boolean escolherCatalogo(boolean clienteExclusivo) {
        if (!clienteExclusivo) {
            return false;
        }

        while (true) {
            try {
                System.out.println("\n--- ESCOLHA A LOJA PARA NAVEGAR ---");
                System.out.println("1 - Catálogo Convencional");
                System.out.println("2 - Catálogo Exclusivo VIP");
                System.out.print("Opção: ");
                int opcaoLoja = ent.nextInt();
                ent.nextLine(); // Limpeza do buffer

                if (opcaoLoja == 1) {
                    return false;
                } else if (opcaoLoja == 2) {
                    return true;
                } else {
                    System.out.println("[X] Opção inválida! Escolha 1 ou 2.");
                }
            } catch (InputMismatchException e) {
                System.out.println("[X] Entrada inválida! Digite apenas o número 1 ou 2.");
                ent.nextLine();
            }
        }
    }

    public static void mostrarCatalogo(boolean vip) {
        ArrayList<Integer> codigos = listaCodigos(vip);
        ArrayList<String> nomes = listaNomes(vip);
        ArrayList<Double> precos = listaPrecos(vip);
        ArrayList<Integer> estoque = listaEstoque(vip);

        System.out.println("\n-----------------------------------------------------------------");
        System.out.printf("   CATÁLOGO - LOJA %s%n", vip ? "EXCLUSIVA (MEMBROS VIP)" : "CONVENCIONAL");
        System.out.println("-----------------------------------------------------------------");
        System.out.printf("%-8s | %-30s | %-10s | %-8s%n", "Código", "Produto", "Preço", "Estoque");
        System.out.println("-----------------------------------------------------------------");

        for (int i = 0; i < codigos.size(); i++) {
            System.out.printf("%-8d | %-30s | R$ %8.2f | %-8d%n", codigos.get(i), nomes.get(i), precos.get(i), estoque.get(i));
        }
        System.out.println("-----------------------------------------------------------------");
    }

    // Aceita somente o código (só dígitos) ou o nome completo do produto.
    // Retorna o índice do produto no catálogo, ou -1 se não encontrou (a mensagem de erro já é mostrada aqui).
    public static int lerProduto(boolean vip) {
        System.out.print("Digite o código ou o nome completo do produto: ");
        String texto = ent.nextLine().trim();

        if (texto.isEmpty()) {
            System.out.println("\n[X] Entrada inválida! Digite o código ou o nome completo do produto.");
            return -1;
        }

        if (texto.matches("[0-9]+")) {
            // Somente dígitos: é o código do produto
            try {
                int codigo = Integer.parseInt(texto);
                int indice = listaCodigos(vip).indexOf(codigo);
                if (indice == -1) {
                    System.out.println("\n[X] Código de produto inválido. Tente novamente.");
                }
                return indice;
            } catch (NumberFormatException e) {
                System.out.println("\n[X] Entrada inválida! O número do código é grande demais.");
                return -1;
            }
        }

        // Qualquer outra coisa: tem que ser o nome por extenso (sem diferenciar maiúsculas)
        ArrayList<String> nomes = listaNomes(vip);
        for (int i = 0; i < nomes.size(); i++) {
            if (nomes.get(i).equalsIgnoreCase(texto)) {
                return i;
            }
        }
        System.out.println("\n[X] Nome não encontrado. Digite o nome completo, como aparece no catálogo, ou o código.");
        return -1;
    }

    public static void escolherProduto(boolean vip, boolean clienteExclusivo) {
        int indice = lerProduto(vip);
        if (indice == -1) {
            return;
        }

        if (listaEstoque(vip).get(indice) <= 0) {
            System.out.println("\n[X] Produto esgotado no momento! Escolha outro modelo.");
            return;
        }

        int opcaoCor = escolherCor(clienteExclusivo);
        int tamanho = escolherTamanho();
        adicionarAoCarrinho(indice, vip, opcaoCor, tamanho);
    }

    public static int escolherCor(boolean clienteExclusivo) {
        // A última cor (Custom Especial) é exclusiva de quem tem o cartão
        int totalCores = clienteExclusivo ? coresDisponiveis.length : coresDisponiveis.length - 1;

        System.out.println("\n--- ESCOLHA A COR DO MODELO ---");
        for (int i = 0; i < totalCores; i++) {
            System.out.printf("%d - %s%s%n", (i + 1), coresDisponiveis[i],
                    (i == coresDisponiveis.length - 1) ? " (VIP)" : "");
        }

        return lerNumeroNoIntervalo("Selecione a opção de cor: ", 1, totalCores,
                "Opção de cor inválida! Escolha de 1 a " + totalCores + ".",
                "Entrada inválida! Digite o número da cor.");
    }

    // Numeração disponível: 34 ao 45
    public static int escolherTamanho() {
        return lerNumeroNoIntervalo("Digite o tamanho (34 a 45): ", 34, 45,
                "Tamanho inválido! Escolha de 34 a 45.",
                "Entrada inválida! Digite o número do tamanho.");
    }

    public static void adicionarAoCarrinho(int indice, boolean vip, int opcaoCor, int tamanho) {
        String nomeComCor = listaNomes(vip).get(indice) + " [" + coresDisponiveis[opcaoCor - 1] + "] Tam. " + tamanho;

        listaEstoque(vip).set(indice, listaEstoque(vip).get(indice) - 1);

        carrinhoNomes.add(nomeComCor);
        carrinhoPrecos.add(listaPrecos(vip).get(indice));
        carrinhoCodigos.add(listaCodigos(vip).get(indice));
        carrinhoVip.add(vip);

        System.out.printf("%n[OK] %s, foi adicionado ao carrinho!%n", nomeComCor);
    }

    public static void mostrarItensDoCarrinho() {
        for (int i = 0; i < carrinhoNomes.size(); i++) {
            System.out.printf("Item %-2d: %-50s | R$ %7.2f%n", (i + 1), carrinhoNomes.get(i), carrinhoPrecos.get(i));
        }
    }

    public static double calcularSubtotal() {
        double soma = 0.0;
        for (int i = 0; i < carrinhoPrecos.size(); i++) {
            soma += carrinhoPrecos.get(i);
        }
        return arredondar(soma);
    }

    // Remoção de itens do carrinho antes de fechar o pedido
    public static void removerItensDoCarrinho() {
        while (!carrinhoNomes.isEmpty()) {
            System.out.println("\n--- ITENS NO CARRINHO ---");
            mostrarItensDoCarrinho();

            if (!lerSimNao("\nDeseja remover algum item do carrinho? (sim [s] / não [n]): ")) {
                break;
            }

            try {
                System.out.print("Digite o número do item a remover: ");
                int numeroItem = ent.nextInt();
                ent.nextLine(); // Limpeza do buffer

                if (numeroItem < 1 || numeroItem > carrinhoNomes.size()) {
                    System.out.println("[X] Número de item inválido!");
                } else {
                    removerItem(numeroItem - 1);
                }
            } catch (InputMismatchException e) {
                System.out.println("[X] Entrada inválida! Digite o número do item.");
                ent.nextLine();
            }
        }
    }

    public static void removerItem(int indiceItem) {
        // Devolve a unidade ao estoque do catálogo de origem
        boolean vip = carrinhoVip.get(indiceItem);
        int indiceEstoque = listaCodigos(vip).indexOf(carrinhoCodigos.get(indiceItem));
        if (indiceEstoque != -1) {
            listaEstoque(vip).set(indiceEstoque, listaEstoque(vip).get(indiceEstoque) + 1);
        }

        System.out.printf("%n[OK] %s, foi removido do carrinho!%n", carrinhoNomes.get(indiceItem));

        carrinhoNomes.remove(indiceItem);
        carrinhoPrecos.remove(indiceItem);
        carrinhoCodigos.remove(indiceItem);
        carrinhoVip.remove(indiceItem);
    }

    // =====================================================================
    // PEDIDO E PAGAMENTO
    // =====================================================================

    public static void finalizarPedido(boolean clienteExclusivo) {
        // Cada valor é arredondado para centavos, para a soma nunca divergir do que aparece na tela
        double subtotal = calcularSubtotal();
        double descontoVip = clienteExclusivo ? aliquotaVip : 0.0;

        double valorImposto = arredondar(subtotal * aliquotaImposto);
        double valorTaxa = arredondar(subtotal * aliquotaTaxa);
        double valorDesconto = arredondar(subtotal * descontoVip);
        double totalFinal = arredondar(subtotal + valorImposto + valorTaxa - valorDesconto);

        System.out.println("\n=========================================================");
        System.out.println("                    RESUMO DO PEDIDO                     ");
        System.out.println("=========================================================");

        if (carrinhoNomes.isEmpty()) {
            System.out.println("Nenhum item foi adicionado ao carrinho.");
            return;
        }

        mostrarItensDoCarrinho();

        System.out.println("---------------------------------------------------------");
        System.out.printf("Subtotal:               R$%8.2f%n", subtotal);
        System.out.printf("%-24sR$%8.2f%n", String.format("Impostos (%.2f%%):", aliquotaImposto * 100), valorImposto);
        System.out.printf("%-24sR$%8.2f%n", String.format("Taxas (%.2f%%):", aliquotaTaxa * 100), valorTaxa);
        if (clienteExclusivo) {
            System.out.printf("%-23s-R$%8.2f%n", String.format("Desconto VIP (%.2f%%):", aliquotaVip * 100), valorDesconto);
        }
        System.out.println("---------------------------------------------------------");
        System.out.printf("TOTAL A PAGAR:          R$%8.2f%n", totalFinal);
        System.out.println("---------------------------------------------------------");

        int formaPagamento = escolherFormaPagamento();
        double totalComPagamento = processarPagamento(formaPagamento, totalFinal);
        registrarVenda(formaPagamento, totalComPagamento, valorImposto, valorTaxa);
    }

    public static int escolherFormaPagamento() {
        System.out.println("\n=========================================================");
        System.out.println("                 FORMA DE PAGAMENTO                      ");
        System.out.println("=========================================================");
        System.out.printf("1 - PIX (%.2f%% de desconto adicional)%n", aliquotaPix * 100);
        System.out.println("2 - Cartão de Crédito (até 12x)");
        System.out.println("3 - Cartão de Débito");
        System.out.println("4 - Boleto Bancário");

        int formaPagamento = 0;
        while (formaPagamento < 1 || formaPagamento > 4) {
            try {
                System.out.print("Escolha a opção de pagamento: ");
                formaPagamento = ent.nextInt();
                ent.nextLine(); // Limpeza do buffer
                if (formaPagamento < 1 || formaPagamento > 4) {
                    System.out.println("Opção inválida! Escolha uma opção de 1 a 4.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Entrada inválida! Selecionando opção PIX por padrão.");
                ent.nextLine();
                formaPagamento = 1;
            }
        }
        return formaPagamento;
    }

    // Executa o pagamento escolhido e devolve o valor realmente cobrado
    public static double processarPagamento(int formaPagamento, double totalFinal) {
        double totalComPagamento = totalFinal;

        switch (formaPagamento) {
            case 1: {
                double descontoPix = arredondar(totalFinal * aliquotaPix);
                totalComPagamento = arredondar(totalFinal - descontoPix);
                System.out.printf("%n>> Desconto de %.2f%% no PIX aplicado!%n", aliquotaPix * 100);
                System.out.printf(">> TOTAL COM DESCONTO PIX: R$ %.2f%n", totalComPagamento);
                System.out.println(">> Chave PIX: 123e4567-e89b-12d3-a456-426614174000");
                System.out.println(">> Pagamento confirmado com sucesso!");
                break;
            }
            case 2: {
                int numeroParcelas = lerNumeroNoIntervalo("Digite o número de parcelas (1x a 12x): ", 1, 12,
                        "Número de parcelas inválido! Escolha de 1 a 12.",
                        "Entrada inválida! Digite um número de 1 a 12.");
                double valorParcela = arredondar(totalComPagamento / numeroParcelas);
                System.out.printf("%n>> Pagamento parcelado em %dx de R$ %.2f%n", numeroParcelas, valorParcela);
                System.out.println(">> Transação no Cartão de Crédito APROVADA!");
                break;
            }
            case 3:
                System.out.println("\n>> Aproxime ou insira o cartão na maquininha...");
                System.out.println(">> Transação no Cartão de Débito APROVADA!");
                break;
            case 4:
                System.out.println("\n>> Código de barras: 34191.09008 61234.567890 12345.678901 1 " + totalFinal);
                System.out.printf(">> Boleto gerado no valor de R$ %.2f. Aguardando pagamento.%n", totalComPagamento);
                break;
            default:
                System.out.println("\n>> Opção padrão registrada. Pagamento concluído!");
                break;
        }
        return totalComPagamento;
    }

    // Acumula para o fechamento do dia (valor realmente cobrado, já com o desconto do PIX).
    // Boleto só entra no faturamento quando for pago, então fica em um acumulador separado.
    public static void registrarVenda(int formaPagamento, double totalComPagamento, double valorImposto, double valorTaxa) {
        if (formaPagamento == 4) {
            boletosAguardandoDia = arredondar(boletosAguardandoDia + totalComPagamento);
        } else {
            faturamentoTotalDia = arredondar(faturamentoTotalDia + totalComPagamento);
            impostoVendasDia = arredondar(impostoVendasDia + valorImposto);
            taxasVendasDia = arredondar(taxasVendasDia + valorTaxa);
            itensVendidosDia += carrinhoNomes.size();
            vendasPagasDia++;
        }
        quantidadeVendasDia++;
    }

    // =====================================================================
    // FECHAMENTO DO DIA
    // =====================================================================

    public static void encerrarExpediente() {
        // Soma o dia no acumulado (que não zera e fica salvo no arquivo)
        acumVendas += quantidadeVendasDia;
        acumFaturamento = arredondar(acumFaturamento + faturamentoTotalDia);
        acumImpostoVendas = arredondar(acumImpostoVendas + impostoVendasDia);
        acumTaxas = arredondar(acumTaxas + taxasVendasDia);
        acumItens += itensVendidosDia;
        acumVendasPagas += vendasPagasDia;

        // CBS e IBS são calculados uma única vez, sobre o faturamento acumulado salvo (e não dia a dia)
        double cbsBruta = arredondar(acumFaturamento * aliquotaCbsBruta);    // CBS na Nota Fiscal de Venda
        double cbsEfetiva = arredondar(acumFaturamento * aliquotaCbsEfetiva); // Impacto real da CBS
        double ibs = arredondar(acumFaturamento * aliquotaIbs);
        double totalImpostos = arredondar(acumImpostoVendas + cbsEfetiva + ibs);
        double receitaLiquida = arredondar(acumFaturamento - cbsEfetiva - ibs);

        // Lucro: desconta também os impostos das vendas, as taxas e os custos da tabela da proposta
        double custoProdutos = arredondar(acumItens * (custoCompraUnidade + freteEntradaUnidade));
        double custoPorVenda = arredondar(acumVendasPagas * (embalagemPorVenda + subsidioFretePorVenda));
        double lucroLiquido = arredondar(acumFaturamento - totalImpostos - acumTaxas - custoProdutos - custoPorVenda);

        System.out.println("\n=========================================================");
        System.out.println("       RELATÓRIO FINANCEIRO E DE IMPOSTOS DO DIA         ");
        System.out.println("=========================================================");
        System.out.printf("Total de Vendas Concluídas: %d%n", quantidadeVendasDia);
        System.out.printf("Faturamento Bruto do Dia:   R$ %.2f%n", faturamentoTotalDia);
        System.out.printf("Boletos Aguardando Pagto:   R$ %.2f (não contam no faturamento)%n", boletosAguardandoDia);
        System.out.printf("Imposto sobre as vendas (%.2f%%): R$ %.2f%n", aliquotaImposto * 100, impostoVendasDia);
        System.out.println("---------------------------------------------------------");
        System.out.println("ACUMULADO SALVO (TODOS OS DIAS) E CBS/IBS SOBRE ELE:");
        System.out.printf("Vendas: %d | Faturamento acumulado: R$ %.2f%n", acumVendas, acumFaturamento);
        System.out.printf(">>> Imposto sobre as vendas acumulado:       R$ %.2f%n", acumImpostoVendas);
        System.out.printf(">>> CBS Bruta nas Notas de Venda (%.2f%%):   R$ %.2f%n", aliquotaCbsBruta * 100, cbsBruta);
        System.out.printf(">>> Impacto Efetivo da CBS no Caixa (%.2f%%): R$ %.2f%n", aliquotaCbsEfetiva * 100, cbsEfetiva);
        System.out.printf(">>> IBS (%.2f%%):                             R$ %.2f%n", aliquotaIbs * 100, ibs);
        System.out.println("---------------------------------------------------------");
        System.out.printf("TOTAL DE IMPOSTOS ACUMULADO:              R$ %.2f%n", totalImpostos);
        System.out.printf("RECEITA LIQUIDA ACUMULADA (APOS CBS E IBS): R$ %.2f%n", receitaLiquida);
        System.out.println("---------------------------------------------------------");
        System.out.println("CUSTOS E LUCRO (ACUMULADO):");
        System.out.printf(">>> Taxas de cartão/parcelamento/gateway:    R$ %.2f%n", acumTaxas);
        System.out.printf(">>> Custo de compra + frete de entrada (%d):  R$ %.2f%n", acumItens, custoProdutos);
        System.out.printf(">>> Embalagem + subsídio de frete (%d vendas): R$ %.2f%n", acumVendasPagas, custoPorVenda);
        System.out.printf("LUCRO LIQUIDO ACUMULADO:                  R$ %.2f%n", lucroLiquido);
        System.out.println("=========================================================");
        System.out.println("     EXPEDIENTE ENCERRADO - OBRIGADO E ATÉ AMANHÃ!       ");
        System.out.println("=========================================================");

        salvarHistorico(cbsBruta, cbsEfetiva, ibs, totalImpostos, receitaLiquida, lucroLiquido);

        // Novo expediente: zera os totais do dia (o estoque, as alíquotas e o acumulado continuam)
        quantidadeVendasDia = 0;
        faturamentoTotalDia = 0.0;
        boletosAguardandoDia = 0.0;
        impostoVendasDia = 0.0;
        taxasVendasDia = 0.0;
        itensVendidosDia = 0;
        vendasPagasDia = 0;
    }

    // Acrescenta o relatório no arquivo relatorios.txt (modo append) nas duas pastas
    public static void salvarHistorico(double cbsBruta, double cbsEfetiva, double ibs, double totalImpostos, double receitaLiquida, double lucroLiquido) {
        String dataHora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        for (int i = 0; i < pastasDestino.length; i++) {
            new File(pastasDestino[i]).mkdirs();
            File arquivoHistorico = new File(pastasDestino[i], "relatorios.txt");
            try (BufferedWriter historico = new BufferedWriter(new FileWriter(arquivoHistorico, StandardCharsets.UTF_8, true))) {
                historico.write("=== Expediente encerrado em " + dataHora + " ===");
                historico.newLine();
                historico.write(String.format("Dia - Vendas: %d | Faturamento: R$ %.2f | Boletos aguardando: R$ %.2f | Imposto das vendas: R$ %.2f",
                        quantidadeVendasDia, faturamentoTotalDia, boletosAguardandoDia, impostoVendasDia));
                historico.newLine();
                historico.write(String.format("Acumulado - Vendas: %d | Faturamento: R$ %.2f | Imposto das vendas: R$ %.2f",
                        acumVendas, acumFaturamento, acumImpostoVendas));
                historico.newLine();
                historico.write(String.format("Sobre o acumulado - CBS bruta (%.2f%%): R$ %.2f | CBS efetiva (%.2f%%): R$ %.2f | IBS (%.2f%%): R$ %.2f",
                        aliquotaCbsBruta * 100, cbsBruta, aliquotaCbsEfetiva * 100, cbsEfetiva, aliquotaIbs * 100, ibs));
                historico.newLine();
                historico.write(String.format("Total de impostos acumulado: R$ %.2f | Receita líquida acumulada: R$ %.2f | Lucro líquido acumulado: R$ %.2f",
                        totalImpostos, receitaLiquida, lucroLiquido));
                historico.newLine();
                historico.newLine();
            } catch (IOException e) {
                System.out.println(">> Aviso: não foi possível gravar o histórico em " + pastasDestino[i] + " (" + e.getMessage() + ")");
            }
        }
    }
                                    }
