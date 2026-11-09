import java.util.ArrayList;
import java.util.InputMismatchException;
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
                "Nike Air Jordan 1", "Nike Air Jordan 4", "Nike Air Jordan 11",
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
            int quantidadeVendasDia = 0;

            // Senha do painel administrativo (troque por outra de sua preferência)
            String senhaAdmin = "admin123";

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
                System.out.print("\nPossui cartão exclusivo da loja? (sim [s] / não [n] / admin [a]): ");
                String possuiCartao = ent.nextLine().trim();

                // Modificar produtos por uma aba de adm
                if (possuiCartao.equalsIgnoreCase("a") || possuiCartao.equalsIgnoreCase("admin")) {
                    System.out.print("Digite a senha do administrador: ");
                    String senhaDigitada = ent.nextLine().trim();
                    boolean emModoAdmin = senhaDigitada.equals(senhaAdmin);
                    if (!emModoAdmin) {
                        System.out.println(">> Senha incorreta! Acesso ao painel administrativo negado.\n");
                    }
                    while (emModoAdmin) {
                        System.out.println("\n=========================================");
                        System.out.println("       PAINEL ADMINISTRATIVO (ADM)       ");
                        System.out.println("=========================================");
                        System.out.println("1 - Alterar Preço de Produto");
                        System.out.println("2 - Repor/Alterar Estoque de Produto");
                        System.out.println("3 - Adicionar Novo Produto ao Catálogo");
                        System.out.println("4 - Remover Produto do Catálogo");
                        System.out.println("5 - Sair do Modo Admin e Ir para a Loja");
                        System.out.print("Escolha uma opção: ");

                        try {
                            int opcaoAdmin = ent.nextInt();
                            ent.nextLine(); // Limpeza do buffer

                            if (opcaoAdmin == 1) {
                                System.out.print("Qual catálogo deseja alterar? (1 - Convencional | 2 - VIP): ");
                                int opcaoCatalogo = ent.nextInt();
                                System.out.print("Digite o código do produto: ");
                                int codigoProdutoAlvo = ent.nextInt();
                                System.out.print("Digite o novo preço: R$ ");
                                double precoProdutoNovo = ent.nextDouble();
                                ent.nextLine(); // Limpeza do buffer

                                if (opcaoCatalogo != 1 && opcaoCatalogo != 2) {
                                    System.out.println(">> Catálogo inválido! Escolha 1 ou 2.");
                                } else if (precoProdutoNovo <= 0) {
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
                                String nomeProdutoNovo = ent.nextLine();
                                System.out.print("Digite o preço: R$ ");
                                double precoProdutoNovo = ent.nextDouble();
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
                                } else if (codigoJaExiste) {
                                    System.out.println(">> Já existe um produto com esse código neste catálogo!");
                                } else if (precoProdutoNovo <= 0 || estoqueProdutoNovo < 0) {
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
                                emModoAdmin = false;
                                System.out.println("\nSaindo do modo ADM...\n");
                            } else {
                                System.out.println(">> Opção inválida!");
                            }
                        } catch (InputMismatchException e) {
                            System.out.println(">> Entrada inválida no painel ADM!\n");
                            ent.nextLine(); // Limpa a entrada inválida
                        }
                    }

                    System.out.print("Possui cartão exclusivo da loja? (sim [s] / não [n]): ");
                    possuiCartao = ent.nextLine().trim();
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
                    try {
                        System.out.print("Digite o código do produto desejado: ");
                        codigoEscolhido = ent.nextInt();
                        ent.nextLine(); // Limpeza do buffer
                        codigoLido = true;
                    } catch (InputMismatchException e) {
                        System.out.println("\n[X] Entrada inválida! O código deve ser um número inteiro.");
                        ent.nextLine(); // Limpa o buffer
                        codigoEscolhido = -1;
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

                                System.out.println("\n--- ESCOLHA A COR DO MODELO ---");
                                for (int indiceCor = 0; indiceCor < coresDisponiveis.length; indiceCor++) {
                                    System.out.printf("%d - %s%n", (indiceCor + 1), coresDisponiveis[indiceCor]);
                                }

                                int opcaoCor = 1;
                                try {
                                    System.out.print("Selecione a opção de cor: ");
                                    opcaoCor = ent.nextInt();
                                    ent.nextLine(); // Limpeza do buffer
                                    if (opcaoCor < 1 || opcaoCor > coresDisponiveis.length) {
                                        System.out.println("Opção de cor inválida, aplicando a cor padrão.");
                                        opcaoCor = 1;
                                    }
                                } catch (InputMismatchException e) {
                                    System.out.println("Opção inválida, aplicando a cor padrão.");
                                    ent.nextLine();
                                    opcaoCor = 1;
                                }

                                String corEscolhida = coresDisponiveis[opcaoCor - 1];
                                String nomeComCor = nomesLoja.get(i) + " [" + corEscolhida + "]";

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

                    System.out.print("\nDeseja continuar a comprar? (sim [s] / não [n]): ");
                    continuar = ent.nextLine().trim();

                } while (continuar.equalsIgnoreCase("s") || continuar.equalsIgnoreCase("sim"));

                // Remoção de itens do carrinho antes de fechar o pedido
                while (!carrinhoNomes.isEmpty()) {
                    System.out.println("\n--- ITENS NO CARRINHO ---");
                    for (int i = 0; i < carrinhoNomes.size(); i++) {
                        System.out.printf("Item %-2d: %-38s | R$ %7.2f%n", (i + 1), carrinhoNomes.get(i), carrinhoPrecos.get(i));
                    }

                    System.out.print("\nDeseja remover algum item do carrinho? (sim [s] / não [n]): ");
                    String respostaRemover = ent.nextLine().trim();

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
                double imposto = 0.04; // 4%
                double taxa = 0.06; // 6%
                double descontoVIP = clienteExclusivo ? 0.05 : 0.0; // 5% VIP

                double valorImposto = subtotal * imposto;
                double valorTaxa = subtotal * taxa;
                double valorDesconto = subtotal * descontoVIP;
                double totalFinal = subtotal + valorImposto + valorTaxa - valorDesconto;

                System.out.println("\n=========================================================");
                System.out.println("                    RESUMO DO PEDIDO                     ");
                System.out.println("=========================================================");

                if (!carrinhoNomes.isEmpty()) {
                    for (int i = 0; i < carrinhoNomes.size(); i++) {
                        System.out.printf("Item %-2d: %-38s | R$ %7.2f%n", (i + 1), carrinhoNomes.get(i), carrinhoPrecos.get(i));
                    }

                    System.out.println("---------------------------------------------------------");
                    System.out.printf("Subtotal:               R$%8.2f%n", subtotal);
                    System.out.printf("Impostos (4%%):          R$%8.2f%n", valorImposto);
                    System.out.printf("Taxas (6%%):             R$%8.2f%n", valorTaxa);

                    if (clienteExclusivo) {
                        System.out.printf("Desconto VIP (5%%):     -R$%8.2f%n", valorDesconto);
                    }

                    System.out.println("---------------------------------------------------------");
                    System.out.printf("TOTAL A PAGAR:          R$%8.2f%n", totalFinal);
                    System.out.println("---------------------------------------------------------");

                    System.out.println("\n=========================================================");
                    System.out.println("                 FORMA DE PAGAMENTO                      ");
                    System.out.println("=========================================================");
                    System.out.println("1 - PIX (5% de desconto adicional)");
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
                            double descontoPix = totalFinal * 0.05;
                            totalComPagamento -= descontoPix;
                            System.out.println("\n>> Desconto de 5% no PIX aplicado!");
                            System.out.printf(">> TOTAL COM DESCONTO PIX: R$ %.2f%n", totalComPagamento);
                            System.out.println(">> Chave PIX: 123e4567-e89b-12d3-a456-426614174000");
                            System.out.println(">> Pagamento confirmado com sucesso!");
                            break;
                        case 2:
                            System.out.print("Digite o número de parcelas (1x a 12x): ");
                            int numeroParcelas = 1;
                            try {
                                numeroParcelas = ent.nextInt();
                                ent.nextLine(); // Limpeza do buffer
                                if (numeroParcelas < 1 || numeroParcelas > 12) {
                                    System.out.println("Número de parcelas inválido! Considerando pagamento à vista (1x).");
                                    numeroParcelas = 1;
                                }
                            } catch (InputMismatchException e) {
                                System.out.println("Entrada inválida! Considerando pagamento à vista (1x).");
                                ent.nextLine();
                            }
                            double valorParcela = totalComPagamento / numeroParcelas;
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

                    // Acumula para o fechamento do dia (valor realmente cobrado, já com o desconto do PIX)
                    faturamentoTotalDia += totalComPagamento;
                    quantidadeVendasDia++;

                } else {
                    System.out.println("Nenhum item foi adicionado ao carrinho.");
                }

                System.out.println("=========================================================");
                System.out.println("        Obrigado por comprar na nossa loja!              ");
                System.out.println("=========================================================");

                // Pergunta final para decidir encerramento do dia
                System.out.print("\nEsta foi a última venda do dia? (sim [s] / não [n]): ");
                String ultimaVenda = ent.nextLine().trim();

                if (ultimaVenda.equalsIgnoreCase("s") || ultimaVenda.equalsIgnoreCase("sim")) {
                    sistemaAtivo = false;

                    // Cálculo dos impostos CBS (Alíquotas do Novo Sistema Tributário)
                    double cbsRetidaVenda = faturamentoTotalDia * 0.0921; // 9,21% na Nota Fiscal de Venda
                    double cbsEfetivaCaixa = faturamentoTotalDia * 0.0368; // 3,68% de impacto real da CBS

                    System.out.println("\n=========================================================");
                    System.out.println("       RELATÓRIO FINANCEIRO E DE IMPOSTOS DO DIA         ");
                    System.out.println("=========================================================");
                    System.out.printf("Total de Vendas Concluídas: %d%n", quantidadeVendasDia);
                    System.out.printf("Faturamento Bruto Total:    R$ %.2f%n", faturamentoTotalDia);
                    System.out.println("---------------------------------------------------------");
                    System.out.println("IMPOSTOS E ALÍQUOTAS CBS (NOVO SISTEMA TRIBUTÁRIO):");
                    System.out.printf("  CBS Bruta nas Notas de Venda (9,21%%):   R$ %.2f%n", cbsRetidaVenda);
                    System.out.printf("  Impacto Efetivo da CBS no Caixa (3,68%%): R$ %.2f%n", cbsEfetivaCaixa);
                    System.out.println("---------------------------------------------------------");
                    System.out.printf("GASTO TOTAL COM CBS NO DIA:               R$ %.2f%n", cbsEfetivaCaixa);
                    System.out.printf("RECEITA LIQUIDA DO DIA (APOS CBS):        R$ %.2f%n", faturamentoTotalDia - cbsEfetivaCaixa);
                    System.out.println("=========================================================");
                    System.out.println("     EXPEDIENTE ENCERRADO - OBRIGADO E ATÉ AMANHÃ!       ");
                    System.out.println("=========================================================");
                } else {
                    System.out.println("\n>> Reiniciando o sistema para o próximo cliente...\n");
                }
            }
        }
    }
}
