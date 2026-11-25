# 👟 Projeto Hermes

Sistema de loja de tênis em **Java** (aplicação de console) para vender produtos, controlar o estoque e calcular impostos, com uma área exclusiva para clientes que possuem o cartão da loja. O projeto também inclui uma **demonstração web** (HTML, CSS e JavaScript) da interface.

> Projeto Integrador desenvolvido em grupo no curso de Análise e Desenvolvimento de Sistemas (ADS) do Senac Nações Unidas, em 2026. Projeto acadêmico.

## 📌 Sobre o projeto

A loja funciona com **três perfis de uso**:

| Perfil | O que faz |
|---|---|
| **Convencional** | Qualquer cliente consulta o catálogo, escolhe cor e tamanho e realiza a compra. |
| **VIP** | O cartão libera o catálogo exclusivo, a cor Custom Especial e 5% de desconto. |
| **ADM** | Com senha, altera preços, estoque, produtos e alíquotas. |

São 20 modelos, 5 cores, numeração de 34 a 45 e pagamento por PIX, crédito, débito e boleto.

## ✨ Funcionalidades

- **Fluxo de venda completo:** cartão, catálogo, produto (por código ou nome), cor, tamanho, carrinho e pagamento. Cada etapa é um método, e entradas inválidas geram um aviso e uma nova solicitação.
- **Estoque integrado ao carrinho:** o estoque é reduzido quando o item entra no carrinho e restaurado quando ele é removido. Produtos esgotados são bloqueados antes da escolha de cor e tamanho.
- **Cálculo de valores:** imposto de 4%, taxa de 6% e desconto VIP de 5%, todos sobre o subtotal. O PIX concede mais 5% de desconto sobre o total.
- **Relatório do dia:** ao encerrar o expediente, o sistema gera um relatório com faturamento, CBS, IBS e lucro. As alíquotas podem ser editadas pelo ADM.
- **Salvamento com arquivo temporário:** os dados são gravados em duas cópias (pasta principal e backup) a cada atendimento. A escrita usa um arquivo `.tmp` e substitui o anterior quando não ocorre falha de escrita; a validação do conteúdo é feita na leitura dos dados, não antes da substituição.
- **Tratamento de erros:** texto no lugar de número, preço inválido ou entrada interrompida não derrubam o programa.

### Exemplo de cálculo

Cliente VIP, pagando com PIX:

| Item | Valor |
|---|---:|
| Subtotal | R$ 1.399,99 |
| Imposto (4%) | + R$ 56,00 |
| Taxa (6%) | + R$ 84,00 |
| Desconto VIP (5%) | − R$ 70,00 |
| **Total a pagar** | **R$ 1.469,99** |
| Com PIX (−5%) | **R$ 1.396,49** |

Esse exemplo foi reproduzido compilando e executando a aplicação com um fluxo de venda simulado. As alíquotas de CBS/IBS do relatório são premissas adotadas pelo grupo: o próprio código marca a CBS efetiva como premissa sem fonte oficial, a revisar. Elas não devem ser tratadas como orientação tributária.

## 🧠 Como o código funciona

- **Uma classe, vários métodos:** a aplicação Java não usa orientação a objetos. A lógica fica em métodos `public static` e variáveis da classe.
- **Listas paralelas:** preço, nome, código e estoque ficam em `ArrayList`s com o mesmo índice, e um `boolean vip` define qual catálogo cada método usa.
- **Laço principal:** um `while` atende um cliente por vez, do cartão ao pagamento, e salva os dados a cada atendimento.
- **Persistência em texto:** um registro por linha, com etiquetas (`N`, `V`, `ALIQ`, `DIA`, `ACUM`) que identificam o conteúdo.

## 🛠️ Tecnologias

- **Java:** aplicação de console, com `ArrayList` e manipulação de arquivos (dados e backups).
- **HTML5, CSS3 e JavaScript:** demonstração web da interface, com **Tailwind CSS**, **Lucide Icons**, Google Fonts e uma imagem de QR Code carregados por serviços externos (CDN/API).
- **Git e GitHub:** versionamento e publicação do projeto.

## ▶️ Como executar

### Aplicação Java (console)

**Pré-requisito:** JDK instalado e configurado.

```bash
# Clone o repositório e entre na pasta
git clone https://github.com/teles-hub/Proposta-Pi.git
cd Proposta-Pi

# Compile e execute
javac ProjetoPi.java
java ProjetoPi
```

### Teste automatizado

O teste de regressão compila o Java, executa o fluxo de venda usado na tabela deste README e confirma os valores e os arquivos salvos nas duas pastas. Ele usa diretórios temporários e não altera o repositório.

```bash
py -3 -m unittest discover -s tests -v
```

### Demonstração web

Abra o arquivo `projeto_Hermes(v2).html` em um navegador, ou use a extensão Live Server no Visual Studio Code. Como as bibliotecas vêm por CDN, é preciso estar conectado à internet.

> A versão web é apenas uma demonstração de interface (front-end). Ela não tem servidor, autenticação segura nem integração com pagamento, e as operações são simuladas no navegador. Ela não está conectada à aplicação Java. As credenciais presentes no código são fixas e servem somente para a demonstração; não devem ser reutilizadas. A demonstração web também diverge da aplicação Java em alguns valores de CBS e não calcula IBS no relatório, apesar dos comentários internos que dizem seguir as mesmas regras.

## 📊 Status

Projeto acadêmico encerrado. A aplicação Java foi compilada e executada com um fluxo de venda simulado; não há testes automatizados no repositório.

## 👥 Equipe

| Integrante | Contribuição |
|---|---|
| **Cauê Teles** | Aplicação em Java |
| **Gabriel Roque** | Interface web (HTML) |
| **Yuri Vasconcellos** | Pesquisa de mercado e de alíquotas |
| **Murilo H. P.** | Pesquisa de mercado e de alíquotas |

**Orientador:** Prof. Marcus Gália
