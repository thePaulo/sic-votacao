# voting-system

O sistema permite a criação de sessões de voto dada uma pauta através de uma API rest, o sistema usa de CPFs válidos e também pode determinar um tempo limite de votação

## O que foi implementado

* Cadastrar nova pauta
* Abrir uma sessão de votos limitada por tempo customizável
* Votação
* Amostragem da quantidade de votos positivos/negativos numa pauta
* Requisição de API externa de CPFs
* Mensageria e fila
* Possível versionamento da API

## O que não foi feito

* Processamento de larga escala de dados

Obs: até poderia ter começado a procurar mais sobre o Spring Batch para colocar, mas acredito que iria ser muito "over engineering" no meu código

## Dependências

* Java >= 11
* Spring
* RabbitMQ
* ERlang ( necessário baixar caso tenha utilizado Windows como neste projeto )
* PostgreSql

## Como rodar

* Baixe e instale o ERlang http://erlang.org/download/otp_win64_22.3.exe
* Baixe e instale RabbitMQ https://github.com/rabbitmq/rabbitmq-server/releases/download/v3.8.8/rabbitmq-server-3.8.8.exe
* Vá para o diretório do RabbitMQ C:\Program Files\RabbitMQ Server\rabbitmq_server-3.8.3\sbin
* No terminal use o comando: rabbitmq-plugins enable rabbitmq_management
* Vá no navegador para o endereço: http://localhost:15672/ para chegar no RabbitMQ Dashboard
* Também é possível ir pelo http://127.0.0.1:15672
* Login e senha são: guest
* Você deverá ver a tela do RabbitMQ após tudo isso

Créditos da explicação do setup para:  https://github.com/Java-Techie-jt/springboot-rabbitmq-example 

* No servidor do Postgres, você deverá manualmente criar um banco com o nome: voting
Ou pode ser algum outro nome qualquer e aí deverá ser alterado de acordo no arquivo `application.properties`
* Configure no application.properties o seu usuário/senha do postgres

A partir daí então você já poderá rodar o sistema.

# Sobre o Projeto

Ele roda localmente possuindo 2 urls válidas

* localhost:8080/api/v1/topic
* localhost:8080/api/v1/vote

Essas 2 urls proveem para o sistema os dados respectivos às pautas do sistema e aos votos do sistema respectivamente.

Para cadastrar uma pauta, deverá ser utilizado num request post na url `topic` a seguinte estrutura:

```shell
{
    "id":1,
    "description":"new topic",
    "timeLimit":30
}
```

Com id indicando o id da pauta, a descrição da pauta e o tempo limite de abertura da pauta em segundos ( caso não seja informado será aplicado 60 segundos )

Após uma pauta cadastrada, poderá ser votado nesta pauta com um post na url `vote` com a seguinte estrutura

```shell
{
    "id":1,
    "topicId":1,
    "associateId":62289608068,
    "value":0
}
```
O "id" representa o id do voto, o "topicId" representa o id da pauta, o "associateId" representa um CPF válido e o "value" representa o booleano do voto (SIM/NÃO) 

As informações sobre todos os votos e pautas do sistema estão disponíveis após cadastrados nas urls.

# Sobre os testes

Os testes foram feitos em maior parte utilizando-se dos endpoints do sistema, portanto por exemplo, no caso do "testNegativeVote", como há uma chance
de aconter um erro na obtenção do CPF pela API, há chances dele não operar corretamente também.

Pode ser conferido este teste passando na imagem a seguir:
![alt text](https://github.com/thePaulo/voting-system/blob/main/passing%20tests.PNG?raw=true)
