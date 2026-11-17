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

    public static void main(String[] args) {
        try (Scanner ent = new Scanner(System.in)) {

            // Cartões válidos no sistema (tem que definir a quantidade de cartões válidos, por isso usei short que é menor que int)
            ArrayList<Short> cartoesNoSistema = new ArrayList<>();
            for (short i = 1; i <= 10; i++) {
                cartoesNoSistema.add(i);
            }

            // Catálogo normal
            ArrayList<Integer> codigosNormal = new ArrayList<>();
            ArrayList<String> nomesNormal = new ArrayList<>();
            ArrayList<Double> precosNormal = new ArrayList<>();

            // Preenchendo o catálogo normal
            int[] codigosNormalInicializador = {100, 101, 102, 103, 104, 105, 106, 107, 108, 109};
            String[] nomesNormalInicializador = {
                "Nike Air Force 1", "Nike Air Max 1", "Nike Air Max 90",
                "Nike Air Max 270", "Nike Dunk Low", "Nike Dunk High",
                "Nike Pegasus 41", "Nike Vomero 5", "Nike Air Huarache",
                "Nike React Infinity Run"
            };
            double[] precosNormalInicializador = {759.99, 1044.99, 769.99, 899.99, 854.99, 598.49, 661.67, 1234.99, 702.99, 1135.24};

            for (int i = 0; i < codigosNormalInicializador.length; i++) {
                codigosNormal.add(codigosNormalInicializador[i]);
                nomesNormal.add(nomesNormalInicializador[i]);
                precosNormal.add(precosNormalInicializador[i]);
            }

            // Catalogo Exclusivo
            ArrayList<Integer> codigosVip = new ArrayList<>();
            ArrayList<String> nomesVip = new ArrayList<>();
            ArrayList<Double> precosVip = new ArrayList<>();

            // Preenchendo o catálogo exclusivo
            int[] codigosVipInicializador = {200, 201, 202, 203, 204, 205, 206, 207, 208, 209};
            String[] nomesVipInicializador = {
                "Nike Air Max 95", "Nike Air Max 97", "Nike Air Max Plus (TN)",
                "Nike Air Jordan 1 Mid", "Nike Air Jordan 4", "Nike Air Jordan 11",
                "Nike Blazer Mid", "Nike Cortez", "Nike P-6000", "Nike LeBron 22"
            };
            double[] precosVipInicializador = {1399.99, 1424.99, 1329.99, 1499.99, 1709.99, 1709.99, 885.15, 645.99, 854.99, 1519.99};

            for (int i = 0; i < codigosVipInicializador.length; i++) {
                codigosVip.add(codigosVipInicializador[i]);
                nomesVip.add(nomesVipInicializador[i]);
                precosVip.add(precosVipInicializador[i]);
            }

            // Controle de estoque
            ArrayList<Integer> estoqueNormal = new ArrayList<>();
            ArrayList<Integer> estoqueVip = new ArrayList<>();

            // Preenchendo o estoque inicial (5 unidades para cada produto por padrão)
            for (int i = 0; i < codigosNormalInicializador.length; i++) {
                estoqueNormal.add(5);
            }
            for (int i = 0; i < codigosVipInicializador.length; i++) {
                estoqueVip.add(5);
            }

            // Modelo por cor
            String[] coresDisponiveis = {"Preto/Branco", "Branco Total", "Vermelho/Preto", "Azul/Cinza", "Custom Especial"};

            // Acumuladores para o relatório final de gastos e impostos do dia
            double faturamentoTotalDia = 0.0;
            double boletosAguardandoDia = 0.0; // Boleto só vira faturamento quando for pago, por isso fica separado
            int quantidadeVendasDia = 0;

            // Alíquotas e descontos (podem ser alterados no painel administrativo)
            double aliquotaImposto = 0.04;      // 4% de imposto
            double aliquotaTaxa = 0.06;         // 6% de taxa
            double aliquotaVip = 0.05;          // 5% de desconto VIP
            double aliquotaPix = 0.05;          // 5% de desconto no PIX
            double aliquotaCbsBruta = 0.0921;   // 9,21% de CBS bruta
            double aliquotaCbsEfetiva = 0.0368; // 3,68% de CBS efetiva

            // Senha do painel administrativo: guardamos o hash (String.hashCode) e não a senha em texto.
            // Para trocar a senha: rode System.out.println("nova senha".hashCode()) e cole o número abaixo.
            int senhaAdminHash = -969161597;

            // ===== ARQUIVOS: pastas onde os dados são salvos =====
            // Pasta principal e pasta de backup. O backup fica na pasta do usuário do Windows (user.home),
            // que o Java descobre sozinho, então funciona em qualquer usuário sem escrever o nome dele aqui.
            // As propriedades "nikestore.principal" e "nikestore.backup" são opcionais: servem só para trocar as pastas em testes.
            String pastaPrincipal = System.getProperty("nikestore.principal", "C:" + File.separator + "NikeStore" + File.separator + "dados");
            String pastaBackup = System.getProperty("nikestore.backup", System.getProperty("user.home") + File.separator + "NikeStoreBackup");
            String[] pastasDestino = {pastaPrincipal, pastaBackup};

            // ===== CARREGAR: se já existir um arquivo salvo (principal ou backup), ele substitui os dados iniciais =====
            boolean dadosCarregados = false;
            boolean encontrouArquivoInvalido = false;
            for (int origem = 0; origem < pastasDestino.length && !dadosCarregados; origem++) {
                File arquivoLeitura = new File(pastasDestino[origem], "catalogo.txt");
                if (!arquivoLeitura.exists()) {
                    continue;
                }

                // Lê tudo para listas temporárias: os dados reais só são trocados se o arquivo estiver completo e válido
                ArrayList<Integer> lerCodigosN = new ArrayList<>();
                ArrayList<String> lerNomesN = new ArrayList<>();
                ArrayList<Double> lerPrecosN = new ArrayList<>();
                ArrayList<Integer> lerEstoqueN = new ArrayList<>();
                ArrayList<Integer> lerCodigosV = new ArrayList<>();
                ArrayList<String> lerNomesV = new ArrayList<>();
                ArrayList<Double> lerPrecosV = new ArrayList<>();
                ArrayList<Integer> lerEstoqueV = new ArrayList<>();
                double[] lerAliquotas = new double[6];
                double[] lerDia = new double[3];
                boolean arquivoValido = true;
                boolean leuAliquotas = false;
                boolean leuDia = false;
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
                            arquivoValido = linha.equals("NIKESTORE;1");
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

                                    if (nomeLido.isEmpty() || !(precoLido > 0 && precoLido < Double.POSITIVE_INFINITY)
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
                            // Alíquotas: imposto;taxa;vip;pix;cbsBruta;cbsEfetiva (todas entre 0 e 1)
                            if (campos.length != 7) {
                                arquivoValido = false;
                            } else {
                                try {
                                    for (int indiceAliquota = 0; indiceAliquota < 6; indiceAliquota++) {
                                        lerAliquotas[indiceAliquota] = Double.parseDouble(campos[indiceAliquota + 1].trim());
                                        if (!(lerAliquotas[indiceAliquota] >= 0 && lerAliquotas[indiceAliquota] <= 1)) {
                                            arquivoValido = false;
                                        }
                                    }
                                    leuAliquotas = true;
                                } catch (NumberFormatException e) {
                                    arquivoValido = false;
                                }
                            }
                        } else if (campos[0].equals("DIA") && !leuDia) {
                            // Totais do dia: vendas;faturamento;boletos aguardando
                            if (campos.length != 4) {
                                arquivoValido = false;
                            } else {
                                try {
                                    int vendasLidas = Integer.parseInt(campos[1].trim());
                                    double faturamentoLido = Double.parseDouble(campos[2].trim());
                                    double boletosLidos = Double.parseDouble(campos[3].trim());
                                    if (vendasLidas < 0 || !(faturamentoLido >= 0 && faturamentoLido < Double.POSITIVE_INFINITY)
                                            || !(boletosLidos >= 0 && boletosLidos < Double.POSITIVE_INFINITY)) {
                                        arquivoValido = false;
                                    } else {
                                        lerDia[0] = vendasLidas;
                                        lerDia[1] = faturamentoLido;
                                        lerDia[2] = boletosLidos;
                                        leuDia = true;
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

                if (arquivoValido && leuAliquotas && leuDia) {
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
                    quantidadeVendasDia = (int) lerDia[0];
                    faturamentoTotalDia = lerDia[1];
                    boletosAguardandoDia = lerDia[2];
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

            // Loop principal do sistema
            boolean sistemaAtivo = true;

            while (sistemaAtivo) {

                ArrayList<String> carrinhoNomes = new ArrayList<>();
                ArrayList<Double> carrinhoPrecos = new ArrayList<>();
                // Guarda de onde veio cada item (para devolver ao estoque se for removido do carrinho)
                ArrayList<Integer> carrinhoCodigos = new ArrayList<>();
                ArrayList<Boolean> carrinhoVip = new ArrayList<>();

                System.out.println("\n=========================================");
                System.out.println("    LOJA DE TÊNIS CUSTOMIZADOS          ");
                System.out.println("=========================================");
                String possuiCartao = "";
                boolean respostaCartaoValida = false;
                while (!respostaCartaoValida) {
                    System.out.print("\nPossui cartão exclusivo da loja? (sim [s] / não [n] / admin [a]): ");
                    possuiCartao = ent.nextLine().trim();
                    respostaCartaoValida = possuiCartao.equalsIgnoreCase("s") || possuiCartao.equalsIgnoreCase("sim")
                            || possuiCartao.equalsIgnoreCase("n") || possuiCartao.equalsIgnoreCase("nao") || possuiCartao.equalsIgnoreCase("não")
                            || possuiCartao.equalsIgnoreCase("a") || possuiCartao.equalsIgnoreCase("admin");
                    if (!respostaCartaoValida) {
                        System.out.println(">> Resposta inválida! Digite s (sim), n (não) ou a (admin).");
                    }
                }

                // Modificar produtos por uma aba de adm
                if (possuiCartao.equalsIgnoreCase("a") || possuiCartao.equalsIgnoreCase("admin")) {
                    boolean emModoAdmin = false;
                    int tentativasSenha = 0;
                    while (!emModoAdmin && tentativasSenha < 3) {
                        System.out.print("Digite a senha do administrador: ");
                        String senhaDigitada = ent.nextLine().trim();
                        if (senhaDigitada.hashCode() == senhaAdminHash) {
                            emModoAdmin = true;
                        } else {
                            tentativasSenha++;
                            System.out.println(">> Senha incorreta! Tentativa " + tentativasSenha + " de 3.");
                        }
                    }
                    if (!emModoAdmin) {
                        System.out.println(">> Acesso ao painel administrativo negado.\n");
                    }
                    while (emModoAdmin) {
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

                        try {
                            int opcaoAdmin = ent.nextInt();
                            ent.nextLine(); // Limpeza do buffer

                            if (opcaoAdmin == 1) {
                                System.out.print("Qual catálogo deseja alterar? (1 - Convencional | 2 - VIP): ");
                                int opcaoCatalogo = ent.nextInt();
                                System.out.print("Digite o código do produto: ");
                                int codigoProdutoAlvo = ent.nextInt();
                                ent.nextLine(); // Limpeza do buffer
                                System.out.print("Digite o novo preço: R$ ");
                                // Aceita ponto ou vírgula, independente da região do computador
                                double precoProdutoNovo = Double.parseDouble(ent.nextLine().trim().replace(',', '.'));

                                if (opcaoCatalogo != 1 && opcaoCatalogo != 2) {
                                    System.out.println(">> Catálogo inválido! Escolha 1 ou 2.");
                                } else if (!(precoProdutoNovo > 0 && precoProdutoNovo < Double.POSITIVE_INFINITY)) {
                                    System.out.println(">> Preço inválido! O preço deve ser maior que zero.");
                                } else {
                                    ArrayList<Integer> listaCodigos = (opcaoCatalogo == 2) ? codigosVip : codigosNormal;
                                    ArrayList<Double> listaPrecos = (opcaoCatalogo == 2) ? precosVip : precosNormal;
                                    int indiceProduto = listaCodigos.indexOf(codigoProdutoAlvo);
                                    if (indiceProduto != -1) {
                                        listaPrecos.set(indiceProduto, precoProdutoNovo);
                                        System.out.println(">> Preço atualizado com sucesso!");
                                    } else {
                                        System.out.println(">> Produto não encontrado!");
                                    }
                                }
                            } else if (opcaoAdmin == 2) {
                                System.out.print("Qual catálogo deseja alterar? (1 - Convencional | 2 - VIP): ");
                                int opcaoCatalogo = ent.nextInt();
                                System.out.print("Digite o código do produto: ");
                                int codigoProdutoAlvo = ent.nextInt();
                                System.out.print("Digite a nova quantidade em estoque: ");
                                int estoqueProdutoNovo = ent.nextInt();
                                ent.nextLine(); // Limpeza do buffer

                                if (opcaoCatalogo != 1 && opcaoCatalogo != 2) {
                                    System.out.println(">> Catálogo inválido! Escolha 1 ou 2.");
                                } else if (estoqueProdutoNovo < 0) {
                                    System.out.println(">> Estoque inválido! A quantidade não pode ser negativa.");
                                } else {
                                    ArrayList<Integer> listaCodigos = (opcaoCatalogo == 2) ? codigosVip : codigosNormal;
                                    ArrayList<Integer> listaEstoque = (opcaoCatalogo == 2) ? estoqueVip : estoqueNormal;
                                    int indiceProduto = listaCodigos.indexOf(codigoProdutoAlvo);
                                    if (indiceProduto != -1) {
                                        listaEstoque.set(indiceProduto, estoqueProdutoNovo);
                                        System.out.println(">> Estoque atualizado com sucesso!");
                                    } else {
                                        System.out.println(">> Produto não encontrado!");
                                    }
                                }
                            } else if (opcaoAdmin == 3) {
                                System.out.print("Em qual catálogo deseja adicionar? (1 - Convencional | 2 - VIP): ");
                                int opcaoCatalogo = ent.nextInt();
                                System.out.print("Digite o código do novo produto: ");
                                int codigoProdutoNovo = ent.nextInt();
                                ent.nextLine(); // Limpeza do buffer
                                System.out.print("Digite o nome do produto: ");
                                String nomeProdutoNovo = ent.nextLine().trim();
                                System.out.print("Digite o preço: R$ ");
                                // Aceita ponto ou vírgula, independente da região do computador
                                double precoProdutoNovo = Double.parseDouble(ent.nextLine().trim().replace(',', '.'));
                                System.out.print("Digite a quantidade inicial de estoque: ");
                                int estoqueProdutoNovo = ent.nextInt();
                                ent.nextLine(); // Limpeza do buffer

                                ArrayList<Integer> listaCodigosExistentes = (opcaoCatalogo == 2) ? codigosVip : codigosNormal;
                                boolean codigoJaExiste = false;
                                for (int i = 0; i < listaCodigosExistentes.size(); i++) {
                                    if (listaCodigosExistentes.get(i) == codigoProdutoNovo) {
                                        codigoJaExiste = true;
                                    }
                                }

                                if (opcaoCatalogo != 1 && opcaoCatalogo != 2) {
                                    System.out.println(">> Catálogo inválido! Escolha 1 ou 2.");
                                } else if (nomeProdutoNovo.isEmpty()) {
                                    System.out.println(">> O nome do produto não pode ficar em branco!");
                                } else if (nomeProdutoNovo.indexOf(';') != -1) {
                                    System.out.println(">> O nome do produto não pode ter ponto e vírgula (;)!");
                                } else if (codigoJaExiste) {
                                    System.out.println(">> Já existe um produto com esse código neste catálogo!");
                                } else if (!(precoProdutoNovo > 0 && precoProdutoNovo < Double.POSITIVE_INFINITY) || estoqueProdutoNovo < 0) {
                                    System.out.println(">> Preço deve ser maior que zero e estoque não pode ser negativo!");
                                } else {
                                    if (opcaoCatalogo == 2) {
                                        codigosVip.add(codigoProdutoNovo);
                                        nomesVip.add(nomeProdutoNovo);
                                        precosVip.add(precoProdutoNovo);
                                        estoqueVip.add(estoqueProdutoNovo);
                                    } else {
                                        codigosNormal.add(codigoProdutoNovo);
                                        nomesNormal.add(nomeProdutoNovo);
                                        precosNormal.add(precoProdutoNovo);
                                        estoqueNormal.add(estoqueProdutoNovo);
                                    }
                                    System.out.println(">> Novo produto cadastrado com sucesso!");
                                }
                            } else if (opcaoAdmin == 4) {
                                System.out.print("Qual catálogo deseja alterar? (1 - Convencional | 2 - VIP): ");
                                int opcaoCatalogo = ent.nextInt();
                                System.out.print("Digite o código do produto a remover: ");
                                int codigoProdutoAlvo = ent.nextInt();
                                ent.nextLine(); // Limpeza do buffer

                                if (opcaoCatalogo != 1 && opcaoCatalogo != 2) {
                                    System.out.println(">> Catálogo inválido! Escolha 1 ou 2.");
                                } else {
                                    ArrayList<Integer> listaCodigos = (opcaoCatalogo == 2) ? codigosVip : codigosNormal;
                                    ArrayList<String> listaNomes = (opcaoCatalogo == 2) ? nomesVip : nomesNormal;
                                    ArrayList<Double> listaPrecos = (opcaoCatalogo == 2) ? precosVip : precosNormal;
                                    ArrayList<Integer> listaEstoque = (opcaoCatalogo == 2) ? estoqueVip : estoqueNormal;
                                    int indiceProduto = listaCodigos.indexOf(codigoProdutoAlvo);
                                    if (indiceProduto != -1) {
                                        // Remove nas 4 listas paralelas para mantê-las sincronizadas
                                        listaCodigos.remove(indiceProduto);
                                        listaNomes.remove(indiceProduto);
                                        listaPrecos.remove(indiceProduto);
                                        listaEstoque.remove(indiceProduto);
                                        System.out.println(">> Produto removido com sucesso!");
                                    } else {
                                        System.out.println(">> Produto não encontrado!");
                                    }
                                }
                            } else if (opcaoAdmin == 5) {
                                System.out.println("\n--- ALÍQUOTAS E DESCONTOS ATUAIS ---");
                                System.out.printf("1 - Imposto sobre a venda:          %.2f%%%n", aliquotaImposto * 100);
                                System.out.printf("2 - Taxa sobre a venda:             %.2f%%%n", aliquotaTaxa * 100);
                                System.out.printf("3 - Desconto VIP:                   %.2f%%%n", aliquotaVip * 100);
                                System.out.printf("4 - Desconto no PIX:                %.2f%%%n", aliquotaPix * 100);
                                System.out.printf("5 - CBS bruta (nota de venda):      %.2f%%%n", aliquotaCbsBruta * 100);
                                System.out.printf("6 - CBS efetiva (impacto no caixa): %.2f%%%n", aliquotaCbsEfetiva * 100);
                                System.out.print("Qual deseja alterar? (1 a 6): ");
                                int opcaoAliquota = ent.nextInt();
                                ent.nextLine(); // Limpeza do buffer
                                System.out.print("Digite o novo valor em porcentagem (0 a 100): ");
                                // Aceita ponto ou vírgula, independente da região do computador
                                double novaAliquota = Double.parseDouble(ent.nextLine().trim().replace(',', '.')) / 100;

                                if (opcaoAliquota < 1 || opcaoAliquota > 6) {
                                    System.out.println(">> Opção inválida! Escolha de 1 a 6.");
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
                                    } else {
                                        aliquotaCbsEfetiva = novaAliquota;
                                    }
                                    System.out.println(">> Alíquota atualizada com sucesso!");
                                }
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

                    boolean respostaCartaoAdminValida = false;
                    while (!respostaCartaoAdminValida) {
                        System.out.print("Possui cartão exclusivo da loja? (sim [s] / não [n]): ");
                        possuiCartao = ent.nextLine().trim();
                        respostaCartaoAdminValida = possuiCartao.equalsIgnoreCase("s") || possuiCartao.equalsIgnoreCase("sim")
                                || possuiCartao.equalsIgnoreCase("n") || possuiCartao.equalsIgnoreCase("nao") || possuiCartao.equalsIgnoreCase("não");
                        if (!respostaCartaoAdminValida) {
                            System.out.println(">> Resposta inválida! Digite s (sim) ou n (não).");
                        }
                    }
                }

                boolean clienteExclusivo = false;

                // Validação do Cartão
                if (possuiCartao.equalsIgnoreCase("s") || possuiCartao.equalsIgnoreCase("sim")) {
                    while (true) {
                        try {
                            System.out.print("Digite o número do seu cartão (ou 0 para continuar sem cartão): ");
                            long numeroCartao = ent.nextLong();
                            ent.nextLine(); // Limpeza do buffer

                            if (numeroCartao == 0) {
                                System.out.println("\n>> Continuando sem cartão exclusivo.");
                                break;
                            }

                            for (short cartao : cartoesNoSistema) {
                                if (numeroCartao == cartao) {
                                    clienteExclusivo = true;
                                    break;
                                }
                            }

                            if (clienteExclusivo) {
                                System.out.println("\n>> Cartão Exclusivo Validado com Sucesso! <<");
                                break;
                            } else {
                                System.out.println(">> Cartão inválido! Tente novamente.\n");
                            }
                        } catch (InputMismatchException e) {
                            System.out.println(">> Entrada inválida! Por favor, digite apenas números.\n");
                            ent.nextLine(); // Limpa a entrada inválida
                        }
                    }
                }

                double subtotal = 0.0;
                String continuar = "";

                // Loop principal de compras
                do {
                    boolean verCatalogoVip = false;

                    if (clienteExclusivo) {
                        while (true) {
                            try {
                                System.out.println("\n--- ESCOLHA A LOJA PARA NAVEGAR ---");
                                System.out.println("1 - Catálogo Convencional");
                                System.out.println("2 - Catálogo Exclusivo VIP");
                                System.out.print("Opção: ");
                                int opcaoLoja = ent.nextInt();
                                ent.nextLine(); // Limpeza do buffer

                                if (opcaoLoja == 1) {
                                    verCatalogoVip = false;
                                    break;
                                } else if (opcaoLoja == 2) {
                                    verCatalogoVip = true;
                                    break;
                                } else {
                                    System.out.println("[X] Opção inválida! Escolha 1 ou 2.");
                                }
                            } catch (InputMismatchException e) {
                                System.out.println("[X] Entrada inválida! Digite apenas o número 1 ou 2.");
                                ent.nextLine();
                            }
                        }
                    }

                    ArrayList<Integer> codigosLoja = verCatalogoVip ? codigosVip : codigosNormal;
                    ArrayList<String> nomesLoja = verCatalogoVip ? nomesVip : nomesNormal;
                    ArrayList<Double> precosLoja = verCatalogoVip ? precosVip : precosNormal;
                    ArrayList<Integer> estoqueLoja = verCatalogoVip ? estoqueVip : estoqueNormal;

                    System.out.println("\n-----------------------------------------------------------------");
                    System.out.printf("   CATÁLOGO - LOJA %s%n", verCatalogoVip ? "EXCLUSIVA (MEMBROS VIP)" : "CONVENCIONAL");
                    System.out.println("-----------------------------------------------------------------");
                    System.out.printf("%-8s | %-30s | %-10s | %-8s%n", "Código", "Produto", "Preço", "Estoque");
                    System.out.println("-----------------------------------------------------------------");

                    for (int i = 0; i < codigosLoja.size(); i++) {
                        System.out.printf("%-8d | %-30s | R$ %8.2f | %-8d%n", codigosLoja.get(i), nomesLoja.get(i), precosLoja.get(i), estoqueLoja.get(i));
                    }
                    System.out.println("-----------------------------------------------------------------");

                    int codigoEscolhido = -1;
                    boolean codigoLido = false;
                    System.out.print("Digite o código do produto desejado: ");
                    String textoCodigo = ent.nextLine().trim();

                    // Aceita texto junto com o número (ex.: "adsidaoi102"): usa só o último número digitado e ignora o resto
                    int fimNumero = textoCodigo.length();
                    while (fimNumero > 0 && (textoCodigo.charAt(fimNumero - 1) < '0' || textoCodigo.charAt(fimNumero - 1) > '9')) {
                        fimNumero--; // pula o que vier depois do número
                    }
                    int inicioNumero = fimNumero;
                    while (inicioNumero > 0 && textoCodigo.charAt(inicioNumero - 1) >= '0' && textoCodigo.charAt(inicioNumero - 1) <= '9') {
                        inicioNumero--; // volta até o começo do número
                    }

                    if (fimNumero == 0) {
                        System.out.println("\n[X] Entrada inválida! O código deve conter um número.");
                    } else {
                        try {
                            codigoEscolhido = Integer.parseInt(textoCodigo.substring(inicioNumero, fimNumero));
                            codigoLido = true;
                            if (!textoCodigo.equals(String.valueOf(codigoEscolhido))) {
                                System.out.println(">> Código reconhecido: " + codigoEscolhido);
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("\n[X] Entrada inválida! O número do código é grande demais.");
                        }
                    }

                    boolean produtoEncontrado = false;

                    if (codigoLido) {
                        for (int i = 0; i < codigosLoja.size(); i++) {
                            if (codigoEscolhido == codigosLoja.get(i)) {
                                produtoEncontrado = true;

                                if (estoqueLoja.get(i) <= 0) {
                                    System.out.println("\n[X] Produto esgotado no momento! Escolha outro modelo.");
                                    break;
                                }

                                // A última cor (Custom Especial) é exclusiva de quem tem o cartão
                                int totalCoresDisponiveis = clienteExclusivo ? coresDisponiveis.length : coresDisponiveis.length - 1;

                                System.out.println("\n--- ESCOLHA A COR DO MODELO ---");
                                for (int indiceCor = 0; indiceCor < totalCoresDisponiveis; indiceCor++) {
                                    System.out.printf("%d - %s%s%n", (indiceCor + 1), coresDisponiveis[indiceCor],
                                            (indiceCor == coresDisponiveis.length - 1) ? " (VIP)" : "");
                                }

                                int opcaoCor = 0;
                                while (opcaoCor < 1 || opcaoCor > totalCoresDisponiveis) {
                                    System.out.print("Selecione a cor (digite o número ou o nome): ");
                                    String textoCor = ent.nextLine().trim();

                                    // Procura um número no texto (ex.: "cor 2" vira 2): vale o último número digitado
                                    int fimCor = textoCor.length();
                                    while (fimCor > 0 && (textoCor.charAt(fimCor - 1) < '0' || textoCor.charAt(fimCor - 1) > '9')) {
                                        fimCor--; // pula o que vier depois do número
                                    }

                                    if (fimCor > 0) {
                                        // Tem número no texto: usa o número
                                        int inicioCor = fimCor;
                                        while (inicioCor > 0 && textoCor.charAt(inicioCor - 1) >= '0' && textoCor.charAt(inicioCor - 1) <= '9') {
                                            inicioCor--; // volta até o começo do número
                                        }
                                        try {
                                            opcaoCor = Integer.parseInt(textoCor.substring(inicioCor, fimCor));
                                        } catch (NumberFormatException e) {
                                            opcaoCor = 0; // número grande demais
                                        }
                                        if (opcaoCor < 1 || opcaoCor > totalCoresDisponiveis) {
                                            opcaoCor = 0;
                                            System.out.println("Opção de cor inválida! Escolha de 1 a " + totalCoresDisponiveis + ".");
                                        } else if (!textoCor.equals(String.valueOf(opcaoCor))) {
                                            System.out.println(">> Cor reconhecida: " + coresDisponiveis[opcaoCor - 1]);
                                        }
                                    } else {
                                        // Sem número: tenta reconhecer o nome da cor digitado por extenso
                                        String nomeCor = textoCor.toLowerCase();
                                        boolean ehPretoBranco = nomeCor.indexOf("preto") != -1 && nomeCor.indexOf("branco") != -1;
                                        boolean ehBrancoTotal = nomeCor.indexOf("total") != -1
                                                || (nomeCor.indexOf("branco") != -1 && nomeCor.indexOf("preto") == -1);
                                        boolean ehVermelhoPreto = nomeCor.indexOf("vermelho") != -1;
                                        boolean ehAzulCinza = nomeCor.indexOf("azul") != -1 || nomeCor.indexOf("cinza") != -1;
                                        boolean ehCustom = nomeCor.indexOf("custom") != -1 || nomeCor.indexOf("especial") != -1;

                                        // Só vale se o texto combinar com UMA cor (ex.: "preto" sozinho serve para duas, então não vale)
                                        int coresReconhecidas = 0;
                                        int corReconhecida = 0;
                                        if (ehPretoBranco) {
                                            coresReconhecidas++;
                                            corReconhecida = 1;
                                        }
                                        if (ehBrancoTotal) {
                                            coresReconhecidas++;
                                            corReconhecida = 2;
                                        }
                                        if (ehVermelhoPreto) {
                                            coresReconhecidas++;
                                            corReconhecida = 3;
                                        }
                                        if (ehAzulCinza) {
                                            coresReconhecidas++;
                                            corReconhecida = 4;
                                        }
                                        if (ehCustom) {
                                            coresReconhecidas++;
                                            corReconhecida = 5;
                                        }

                                        if (coresReconhecidas != 1) {
                                            System.out.println("Cor não reconhecida! Digite o número ou o nome completo da cor (ex.: Azul/Cinza).");
                                        } else if (corReconhecida > totalCoresDisponiveis) {
                                            System.out.println("A cor Custom Especial é exclusiva para clientes com cartão.");
                                        } else {
                                            opcaoCor = corReconhecida;
                                            System.out.println(">> Cor reconhecida: " + coresDisponiveis[opcaoCor - 1]);
                                        }
                                    }
                                }

                                // Numeração disponível: 34 ao 45
                                int tamanhoEscolhido = 0;
                                while (tamanhoEscolhido < 34 || tamanhoEscolhido > 45) {
                                    System.out.print("Digite o tamanho (34 a 45): ");
                                    String textoTamanho = ent.nextLine().trim();

                                    // Aceita texto junto com o número (ex.: "tam 42" vira 42): vale o último número digitado
                                    int fimTamanho = textoTamanho.length();
                                    while (fimTamanho > 0 && (textoTamanho.charAt(fimTamanho - 1) < '0' || textoTamanho.charAt(fimTamanho - 1) > '9')) {
                                        fimTamanho--; // pula o que vier depois do número
                                    }
                                    int inicioTamanho = fimTamanho;
                                    while (inicioTamanho > 0 && textoTamanho.charAt(inicioTamanho - 1) >= '0' && textoTamanho.charAt(inicioTamanho - 1) <= '9') {
                                        inicioTamanho--; // volta até o começo do número
                                    }

                                    if (fimTamanho == 0) {
                                        System.out.println("Entrada inválida! Digite o número do tamanho.");
                                    } else {
                                        try {
                                            tamanhoEscolhido = Integer.parseInt(textoTamanho.substring(inicioTamanho, fimTamanho));
                                        } catch (NumberFormatException e) {
                                            tamanhoEscolhido = 0; // número grande demais
                                        }
                                        if (tamanhoEscolhido < 34 || tamanhoEscolhido > 45) {
                                            tamanhoEscolhido = 0;
                                            System.out.println("Tamanho inválido! Escolha de 34 a 45.");
                                        } else if (!textoTamanho.equals(String.valueOf(tamanhoEscolhido))) {
                                            System.out.println(">> Tamanho reconhecido: " + tamanhoEscolhido);
                                        }
                                    }
                                }

                                String corEscolhida = coresDisponiveis[opcaoCor - 1];
                                String nomeComCor = nomesLoja.get(i) + " [" + corEscolhida + "] Tam. " + tamanhoEscolhido;

                                estoqueLoja.set(i, estoqueLoja.get(i) - 1);

                                carrinhoNomes.add(nomeComCor);
                                carrinhoPrecos.add(precosLoja.get(i));
                                carrinhoCodigos.add(codigosLoja.get(i));
                                carrinhoVip.add(verCatalogoVip);

                                subtotal += precosLoja.get(i);

                                System.out.printf("%n[OK] %s, foi adicionado ao carrinho!%n", nomeComCor);
                                break;
                            }
                        }

                        if (!produtoEncontrado) {
                            System.out.println("\n[X] Código de produto inválido. Tente novamente.");
                        }
                    }

                    boolean respostaContinuarValida = false;
                    while (!respostaContinuarValida) {
                        System.out.print("\nDeseja continuar a comprar? (sim [s] / não [n]): ");
                        continuar = ent.nextLine().trim();
                        respostaContinuarValida = continuar.equalsIgnoreCase("s") || continuar.equalsIgnoreCase("sim")
                                || continuar.equalsIgnoreCase("n") || continuar.equalsIgnoreCase("nao") || continuar.equalsIgnoreCase("não");
                        if (!respostaContinuarValida) {
                            System.out.println(">> Resposta inválida! Digite s (sim) ou n (não).");
                        }
                    }

                } while (continuar.equalsIgnoreCase("s") || continuar.equalsIgnoreCase("sim"));

                // Remoção de itens do carrinho antes de fechar o pedido
                while (!carrinhoNomes.isEmpty()) {
                    System.out.println("\n--- ITENS NO CARRINHO ---");
                    for (int i = 0; i < carrinhoNomes.size(); i++) {
                        System.out.printf("Item %-2d: %-50s | R$ %7.2f%n", (i + 1), carrinhoNomes.get(i), carrinhoPrecos.get(i));
                    }

                    String respostaRemover = "";
                    boolean respostaRemoverValida = false;
                    while (!respostaRemoverValida) {
                        System.out.print("\nDeseja remover algum item do carrinho? (sim [s] / não [n]): ");
                        respostaRemover = ent.nextLine().trim();
                        respostaRemoverValida = respostaRemover.equalsIgnoreCase("s") || respostaRemover.equalsIgnoreCase("sim")
                                || respostaRemover.equalsIgnoreCase("n") || respostaRemover.equalsIgnoreCase("nao") || respostaRemover.equalsIgnoreCase("não");
                        if (!respostaRemoverValida) {
                            System.out.println(">> Resposta inválida! Digite s (sim) ou n (não).");
                        }
                    }

                    if (!(respostaRemover.equalsIgnoreCase("s") || respostaRemover.equalsIgnoreCase("sim"))) {
                        break;
                    }

                    try {
                        System.out.print("Digite o número do item a remover: ");
                        int numeroItem = ent.nextInt();
                        ent.nextLine(); // Limpeza do buffer

                        if (numeroItem < 1 || numeroItem > carrinhoNomes.size()) {
                            System.out.println("[X] Número de item inválido!");
                        } else {
                            int indiceItem = numeroItem - 1;

                            // Devolve a unidade ao estoque do catálogo de origem
                            ArrayList<Integer> codigosOrigem = carrinhoVip.get(indiceItem) ? codigosVip : codigosNormal;
                            ArrayList<Integer> estoqueOrigem = carrinhoVip.get(indiceItem) ? estoqueVip : estoqueNormal;
                            int indiceEstoque = codigosOrigem.indexOf(carrinhoCodigos.get(indiceItem));
                            if (indiceEstoque != -1) {
                                estoqueOrigem.set(indiceEstoque, estoqueOrigem.get(indiceEstoque) + 1);
                            }

                            System.out.printf("%n[OK] %s, foi removido do carrinho!%n", carrinhoNomes.get(indiceItem));

                            carrinhoNomes.remove(indiceItem);
                            carrinhoPrecos.remove(indiceItem);
                            carrinhoCodigos.remove(indiceItem);
                            carrinhoVip.remove(indiceItem);

                            // Recalcula o subtotal a partir do carrinho
                            subtotal = 0.0;
                            for (int i = 0; i < carrinhoPrecos.size(); i++) {
                                subtotal += carrinhoPrecos.get(i);
                            }
                        }
                    } catch (InputMismatchException e) {
                        System.out.println("[X] Entrada inválida! Digite o número do item.");
                        ent.nextLine();
                    }
                }

                // Cálculo de impostos e descontos locais do cliente
                // Cada valor é arredondado para centavos, para a soma nunca divergir do que aparece na tela
                subtotal = Math.round(subtotal * 100.0) / 100.0;
                double descontoVIP = clienteExclusivo ? aliquotaVip : 0.0;

                double valorImposto = Math.round(subtotal * aliquotaImposto * 100.0) / 100.0;
                double valorTaxa = Math.round(subtotal * aliquotaTaxa * 100.0) / 100.0;
                double valorDesconto = Math.round(subtotal * descontoVIP * 100.0) / 100.0;
                double totalFinal = Math.round((subtotal + valorImposto + valorTaxa - valorDesconto) * 100.0) / 100.0;

                System.out.println("\n=========================================================");
                System.out.println("                    RESUMO DO PEDIDO                     ");
                System.out.println("=========================================================");

                if (!carrinhoNomes.isEmpty()) {
                    for (int i = 0; i < carrinhoNomes.size(); i++) {
                        System.out.printf("Item %-2d: %-50s | R$ %7.2f%n", (i + 1), carrinhoNomes.get(i), carrinhoPrecos.get(i));
                    }

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

                    double totalComPagamento = totalFinal;

                    switch (formaPagamento) {
                        case 1:
                            double descontoPix = Math.round(totalFinal * aliquotaPix * 100.0) / 100.0;
                            totalComPagamento = Math.round((totalFinal - descontoPix) * 100.0) / 100.0;
                            System.out.printf("%n>> Desconto de %.2f%% no PIX aplicado!%n", aliquotaPix * 100);
                            System.out.printf(">> TOTAL COM DESCONTO PIX: R$ %.2f%n", totalComPagamento);
                            System.out.println(">> Chave PIX: 123e4567-e89b-12d3-a456-426614174000");
                            System.out.println(">> Pagamento confirmado com sucesso!");
                            break;
                        case 2:
                            int numeroParcelas = 0;
                            while (numeroParcelas < 1 || numeroParcelas > 12) {
                                try {
                                    System.out.print("Digite o número de parcelas (1x a 12x): ");
                                    numeroParcelas = ent.nextInt();
                                    ent.nextLine(); // Limpeza do buffer
                                    if (numeroParcelas < 1 || numeroParcelas > 12) {
                                        System.out.println("Número de parcelas inválido! Escolha de 1 a 12.");
                                    }
                                } catch (InputMismatchException e) {
                                    System.out.println("Entrada inválida! Digite um número de 1 a 12.");
                                    ent.nextLine();
                                    numeroParcelas = 0;
                                }
                            }
                            double valorParcela = Math.round(totalComPagamento / numeroParcelas * 100.0) / 100.0;
                            System.out.printf("%n>> Pagamento parcelado em %dx de R$ %.2f%n", numeroParcelas, valorParcela);
                            System.out.println(">> Transação no Cartão de Crédito APROVADA!");
                            break;
                        case 3:
                            System.out.println("\n>> Aproxime ou insira o cartão na maquininha...");
                            System.out.println(">> Transação no Cartão de Débito APROVADA!");
                            break;
                        case 4:
                            System.out.println("\n>> Código de barras: 34191.09008 61234.567890 12345.678901 1 987600000");
                            System.out.printf(">> Boleto gerado no valor de R$ %.2f. Aguardando pagamento.%n", totalComPagamento);
                            break;
                        default:
                            System.out.println("\n>> Opção padrão registrada. Pagamento concluído!");
                            break;
                    }

                    // Acumula para o fechamento do dia (valor realmente cobrado, já com o desconto do PIX).
                    // Boleto só entra no faturamento quando for pago, então fica em um acumulador separado.
                    if (formaPagamento == 4) {
                        boletosAguardandoDia = Math.round((boletosAguardandoDia + totalComPagamento) * 100.0) / 100.0;
                    } else {
                        faturamentoTotalDia = Math.round((faturamentoTotalDia + totalComPagamento) * 100.0) / 100.0;
                    }
                    quantidadeVendasDia++;

                } else {
                    System.out.println("Nenhum item foi adicionado ao carrinho.");
                }

                System.out.println("=========================================================");
                System.out.println("        Obrigado por comprar na nossa loja!              ");
                System.out.println("=========================================================");

                // Pergunta final para decidir encerramento do dia
                String ultimaVenda = "";
                boolean respostaUltimaValida = false;
                while (!respostaUltimaValida) {
                    System.out.print("\nEsta foi a última venda do dia? (sim [s] / não [n]): ");
                    ultimaVenda = ent.nextLine().trim();
                    respostaUltimaValida = ultimaVenda.equalsIgnoreCase("s") || ultimaVenda.equalsIgnoreCase("sim")
                            || ultimaVenda.equalsIgnoreCase("n") || ultimaVenda.equalsIgnoreCase("nao") || ultimaVenda.equalsIgnoreCase("não");
                    if (!respostaUltimaValida) {
                        System.out.println(">> Resposta inválida! Digite s (sim) ou n (não).");
                    }
                }

                if (ultimaVenda.equalsIgnoreCase("s") || ultimaVenda.equalsIgnoreCase("sim")) {
                    sistemaAtivo = false;

                    // Cálculo dos impostos CBS (Alíquotas do Novo Sistema Tributário)
                    double cbsRetidaVenda = Math.round(faturamentoTotalDia * aliquotaCbsBruta * 100.0) / 100.0; // CBS na Nota Fiscal de Venda
                    double cbsEfetivaCaixa = Math.round(faturamentoTotalDia * aliquotaCbsEfetiva * 100.0) / 100.0; // Impacto real da CBS

                    System.out.println("\n=========================================================");
                    System.out.println("       RELATÓRIO FINANCEIRO E DE IMPOSTOS DO DIA         ");
                    System.out.println("=========================================================");
                    System.out.printf("Total de Vendas Concluídas: %d%n", quantidadeVendasDia);
                    System.out.printf("Faturamento Bruto Total:    R$ %.2f%n", faturamentoTotalDia);
                    System.out.printf("Boletos Aguardando Pagto:   R$ %.2f (não contam no faturamento)%n", boletosAguardandoDia);
                    System.out.println("---------------------------------------------------------");
                    System.out.println("IMPOSTOS E ALÍQUOTAS CBS (NOVO SISTEMA TRIBUTÁRIO):");
                    System.out.printf("• CBS Bruta nas Notas de Venda (%.2f%%):   R$ %.2f%n", aliquotaCbsBruta * 100, cbsRetidaVenda);
                    System.out.printf("• Impacto Efetivo da CBS no Caixa (%.2f%%): R$ %.2f%n", aliquotaCbsEfetiva * 100, cbsEfetivaCaixa);
                    System.out.println("---------------------------------------------------------");
                    System.out.printf("GASTO TOTAL COM CBS NO DIA:               R$ %.2f%n", cbsEfetivaCaixa);
                    System.out.printf("RECEITA LIQUIDA DO DIA (APOS CBS):        R$ %.2f%n", Math.round((faturamentoTotalDia - cbsEfetivaCaixa) * 100.0) / 100.0);
                    System.out.println("=========================================================");
                    System.out.println("     EXPEDIENTE ENCERRADO - OBRIGADO E ATÉ AMANHÃ!       ");
                    System.out.println("=========================================================");

                    // Histórico: acrescenta o relatório do dia no arquivo relatorios.txt (modo append) nas duas pastas
                    String dataHora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
                    for (int destinoHist = 0; destinoHist < pastasDestino.length; destinoHist++) {
                        new File(pastasDestino[destinoHist]).mkdirs();
                        File arquivoHistorico = new File(pastasDestino[destinoHist], "relatorios.txt");
                        try (BufferedWriter historico = new BufferedWriter(new FileWriter(arquivoHistorico, StandardCharsets.UTF_8, true))) {
                            historico.write("=== Expediente encerrado em " + dataHora + " ===");
                            historico.newLine();
                            historico.write(String.format("Vendas concluídas: %d | Faturamento bruto: R$ %.2f | Boletos aguardando: R$ %.2f",
                                    quantidadeVendasDia, faturamentoTotalDia, boletosAguardandoDia));
                            historico.newLine();
                            historico.write(String.format("CBS bruta (%.2f%%): R$ %.2f | CBS efetiva (%.2f%%): R$ %.2f | Receita líquida: R$ %.2f",
                                    aliquotaCbsBruta * 100, cbsRetidaVenda, aliquotaCbsEfetiva * 100, cbsEfetivaCaixa,
                                    Math.round((faturamentoTotalDia - cbsEfetivaCaixa) * 100.0) / 100.0));
                            historico.newLine();
                            historico.newLine();
                        } catch (IOException e) {
                            System.out.println(">> Aviso: não foi possível gravar o histórico em " + pastasDestino[destinoHist] + " (" + e.getMessage() + ")");
                        }
                    }

                    // Novo expediente: zera os totais do dia (o estoque e as alíquotas continuam)
                    quantidadeVendasDia = 0;
                    faturamentoTotalDia = 0.0;
                    boletosAguardandoDia = 0.0;
                } else {
                    System.out.println("\n>> Reiniciando o sistema para o próximo cliente...\n");
                }

                // ===== SALVAR: grava catálogo, estoque, alíquotas e totais do dia na pasta principal e no backup =====
                // Roda no fim de cada atendimento, então cobre as vendas, as mudanças do admin e o encerramento do dia.
                for (int idxGravar = 0; idxGravar < pastasDestino.length; idxGravar++) {
                    File pastaSalvar = new File(pastasDestino[idxGravar]);
                    pastaSalvar.mkdirs(); // cria a pasta se ela ainda não existir
                    File arquivoTemporario = new File(pastaSalvar, "catalogo.tmp");
                    File arquivoDefinitivo = new File(pastaSalvar, "catalogo.txt");
                    boolean gravou = true;

                    // Grava primeiro em um arquivo temporário: se algo falhar no meio, o arquivo bom anterior não se perde
                    try (BufferedWriter escritor = new BufferedWriter(new FileWriter(arquivoTemporario, StandardCharsets.UTF_8))) {
                        escritor.write("NIKESTORE;1");
                        escritor.newLine();
                        for (int i = 0; i < codigosNormal.size(); i++) {
                            escritor.write("N;" + codigosNormal.get(i) + ";" + nomesNormal.get(i) + ";" + precosNormal.get(i) + ";" + estoqueNormal.get(i));
                            escritor.newLine();
                        }
                        for (int i = 0; i < codigosVip.size(); i++) {
                            escritor.write("V;" + codigosVip.get(i) + ";" + nomesVip.get(i) + ";" + precosVip.get(i) + ";" + estoqueVip.get(i));
                            escritor.newLine();
                        }
                        escritor.write("ALIQ;" + aliquotaImposto + ";" + aliquotaTaxa + ";" + aliquotaVip + ";" + aliquotaPix
                                + ";" + aliquotaCbsBruta + ";" + aliquotaCbsEfetiva);
                        escritor.newLine();
                        escritor.write("DIA;" + quantidadeVendasDia + ";" + faturamentoTotalDia + ";" + boletosAguardandoDia);
                        escritor.newLine();
                    } catch (IOException e) {
                        gravou = false;
                        System.out.println(">> Aviso: não foi possível salvar em " + pastasDestino[idxGravar] + " (" + e.getMessage() + ")");
                    }

                    // Só troca o arquivo antigo pelo novo se a gravação deu certo
                    if (gravou) {
                        if (arquivoDefinitivo.exists()) {
                            arquivoDefinitivo.delete();
                        }
                        if (arquivoTemporario.renameTo(arquivoDefinitivo)) {
                            if (!sistemaAtivo) {
                                System.out.println(">> Dados salvos em: " + arquivoDefinitivo.getPath());
                            }
                        } else {
                            System.out.println(">> Aviso: não foi possível finalizar o arquivo em " + pastasDestino[idxGravar]);
                        }
                    }
                }
            }
        } catch (NoSuchElementException e) {
            // Acontece se a entrada de dados terminar de repente (por exemplo, Ctrl+D ou um arquivo de entrada incompleto)
            System.out.println("\n>> Entrada de dados encerrada. Programa finalizado.");
        }
    }
}
