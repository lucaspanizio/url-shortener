<div>
  <h1 align="left">Url Shortener</h1>
</div>
<p align="left">  Aplicação back-end em Java que utiliza AWS Lambda, Amazon S3 e Amazon API Gateway para encurtamento de URLs. A aplicação gera um código único para cada URL fornecida e também realiza o processo inverso, retornando a URL original com base no código fornecido.</p>

<p align="center">
  <a href="#-pré-requisitos">☝ Pré-requisitos</a>&nbsp;&nbsp;&nbsp;|&nbsp;&nbsp;&nbsp;
  <a href="#-como-configurar">🛠️ Como configurar</a>&nbsp;&nbsp;&nbsp;|&nbsp;&nbsp;&nbsp;
  <a href="#-como-utilizar">⚡Como utilizar</a>&nbsp;&nbsp;&nbsp;|&nbsp;&nbsp;&nbsp;  
  <a href="#-licença">📜 Licença</a>   
  <br><br>

## ☝ Pré-Requisitos

➡️ [Ter uma conta na AWS](https://signin.aws.amazon.com/signin?client_id=arn%3Aaws%3Asignin%3A%3A%3Aconsole%2Fcanvas&redirect_uri=https%3A%2F%2Fconsole.aws.amazon.com%2Fconsole%2Fhome%3FhashArgs%3D%2523%26isauthcode%3Dtrue%26nc2%3Dh_ct%26oauthStart%3D1732579892312%26src%3Dheader-signin%26state%3DhashArgsFromTB_us-east-2_ca9e6689173b76fc&page=resolve&code_challenge=6KycKHmPxwXqzDIu0vUFPo-8kLixJnzAut9Hx4fq-3Y&code_challenge_method=SHA-256&backwards_compatible=true) <br>
➡️ [Ter o JDK 17 ou posterior instalado](https://adoptium.net/) <br>
<br><br>

## 🛠️ Como configurar

Clone este repositório

```bash
git clone https://github.com/lucaspanizio/url-shortener.git
```

Acesse o diretório da aplicação

```bash
cd url-shortener
```

Instale as dependências. Pode fazer isso por meio da IDE ou por meio de linha de comando se tiver o mvn instalado de forma global.

```bash
mvn clean install
```

Compile a aplicação. Novamente isso pode ser feito via IDE ou linha de comando.

```bash
mvn clean package
```

<details>
  <summary>Configurações AWS</summary>

#### 1. Criação de um Bucket S3

Crie um bucket no **Amazon S3** com as **configurações padrão**.

---
  
#### 2. Criação da Função Lambda `ShortUrlGenerator`

Crie uma função Lambda chamada **`ShortUrlGenerator`** com a linguagem **Java**.

⚠️ Certifique-se de selecionar a versão do Java compatível com o projeto. Por padrão, o projeto utiliza **Java 17**, mas você pode ajustar essa configuração no arquivo `pom.xml`.

**Configurações**:
   - Habilite a URL da função Lambda.
   - Tipo de autenticação: **NONE** (sem autenticação).
   - Utilize as demais configurações padrão.
   - Modifique o manipulador da função para:  
   **`com.rocketseat.ShortUrlGenerator::handleRequest`**.
   - Acesse o **papel de execução (IAM Role)** associado à função e **Criar política em linha** com permissões para:
     - `s3:GetObject`
     - `s3:PutObject`
     - `s3:ListBucket`
   - Especifique a ARN do bucket criado na etapa anterior.
   - Compile a aplicação e faça upload do arquivo **`.jar`** gerado.

---

#### 2.1. Criação da Função Lambda `ShortUrlResolver`

Repita os passos acima para criar uma segunda função Lambda chamada **`ShortUrlResolver`**. A única diferença deve ser o **manipulador da função**, para esta função defina-o como:  
**`com.rocketseat.ShortUrlResolver::handleRequest`**.

---

#### 3. Configuração de Variáveis de Ambiente

1. Para ambas as funções Lambda (`ShortUrlGenerator` e `ShortUrlResolver`), adicione uma variável de ambiente chamada **`S3_BUCKET_NAME`** e valor igual ao nome do bucket S3 definido no **passo 1**.

---

#### 4. Configuração do API Gateway

1. Crie uma instância do **Amazon API Gateway** do tipo **HTTP** com as opções padrão.

2. Configure as rotas na API Gateway:
   - **POST /**:
     - Integração: Função Lambda.
     - ARN: **Função Lambda `ShortUrlGenerator`**.
   - **GET /{urlCode}**:
     - Integração: Função Lambda.
     - ARN: **Função Lambda `ShortUrlResolver`**.

3. Conceda permissão em ambas as integrações ao **API Gateway** para invocar as funções Lambda.

</details>
<br>

## ⚡Como utilizar

<div align="center">
  <img src="https://github.com/user-attachments/assets/794a9a4d-7ed3-474b-b01f-172bb0b5979e" alt="POST para criar o short code da URL">
  <p><em>Recebe a URL original e o timestamp em segundos da data de expiração do short code que será gerado.</em></p>
</div>

<div align="center">
  <img src="https://github.com/user-attachments/assets/184fb536-4e64-48a2-9e8c-d3da999167a7" alt="GET para obter a URL original (sem param query)">
  <p><em>Recebe como param path o short code da URL e retorna o link original.</em></p>
</div>

<div align="center">
  <img src="https://github.com/user-attachments/assets/f662932f-f6dc-473b-970a-580925d8c2a0" alt="GET para obter a URL original (com param query)">
  <p><em>Com o param query  redirect=T  você será automaticamente redirecionado para o link original.</em></p>
</div>

<div align="center">
  <img src="https://github.com/user-attachments/assets/345e5e23-b443-4d31-ab7e-6e43e441c7c4" alt="GET com time expirado)">
  <p><em>Após o tempo de expiração estipulado ao criar o short code, a resposta será essa.</em></p>
</div>
<br>

## 📜 Licença

<p>Esse projeto está sob a <a href="https://github.com/lucaspanizio/url-shortener/blob/main/LICENSE">licença MIT</a>.<br>
<img alt="License" src="https://img.shields.io/static/v1?label=license&message=MIT&color=49AA26&labelColor=000000">
</p>

#### Desenvolvido por José Lucas Panizio 🖖

[![Linkedin Badge](https://img.shields.io/badge/-LinkedIn-blue?style=flat-square&logo=Linkedin&logoColor=white&link=https://www.linkedin.com/in/lucaspanizio/)](https://www.linkedin.com/in/lucaspanizio/)
[![Gmail Badge](https://img.shields.io/badge/-Gmail-ff0000?style=flat-square&labelColor=ff0000&logo=gmail&logoColor=white&link=mailto:lucaspanizio@gmail.com)](mailto:lucaspanizio@gmail.com)
