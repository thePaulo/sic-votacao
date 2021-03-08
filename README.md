# sic-votacao

O sistema permite a criação de sessões de voto dada uma pauta através de uma API rest, o sistema usa de CPFs válidos dos associados para permitir o voto e também pode determinar um tempo limite de votação

## Pode checar o resultado do projeto com o deploy na Amazon ou no Heroku por meio dos links

* http://paulovsicvoting-env.eba-uiydnvry.us-east-2.elasticbeanstalk.com
* http://sicvotacao.herokuapp.com

## O que foi implementado

* Cadastrar nova pauta
* Abrir uma sessão de votos limitada por tempo customizável
* Votação
* Amostragem da quantidade de votos positivos/negativos numa pauta
* Requisição de API externa de CPFs
* Mensageria e fila
* Possível versionamento da API
* Performace sob várias requisições simultâneas

## Dependências

* Java >= 11
* Spring Boot
* RabbitMQ
* ERlang
* PostgreSql

## Como rodar

* Baixe e instale o ERlang http://erlang.org/download/otp_win64_22.3.exe
* Baixe e instale RabbitMQ https://github.com/rabbitmq/rabbitmq-server/releases/download/v3.8.8/rabbitmq-server-3.8.8.exe
* Baixe e instale o plugin de mensagens com delay do rabbitmq e coloque-o na pasta sbin https://github.com/rabbitmq/rabbitmq-delayed-message-exchange/releases/download/3.8.9/rabbitmq_delayed_message_exchange-3.8.9-0199d11c.ez
* Vá para o diretório do RabbitMQ: RabbitMQ Server\rabbitmq_server-3.8.3\sbin
* No terminal use o comando: rabbitmq-plugins enable rabbitmq_management
* E o comando : rabbitmq-plugins enable rabbitmq_delayed_message_exchange
* Vá no navegador para o endereço: http://localhost:15672/ para chegar no RabbitMQ Dashboard
* Também é possível ir pelo http://127.0.0.1:15672
* Login e senha são: `guest`
* Você deverá ver a tela de gerenciamento do RabbitMQ após tudo isso



* No servidor do Postgres caso queira rodar localmente, você deverá manualmente criar um banco com o nome: voting
Ou pode ser algum outro nome qualquer e aí deverá ser alterado de acordo no arquivo `application.properties`
* Configure no application.properties o seu usuário/senha do postgres

A partir daí então você já poderá rodar o sistema.

# Sobre o Projeto

Ele possuí 2 urls disponíveis

* localhost:8080/api/v1/topic
* localhost:8080/api/v1/vote

( lembrando que isso caso esteja rodando localmente, como dito no começo da descrição, esta aplicação está rodando na Amazon pelo link http://paulovsicvoting-env.eba-uiydnvry.us-east-2.elasticbeanstalk.com)

Essas 2 urls proveem para o sistema os dados respectivos às pautas do sistema e aos votos do sistema respectivamente.
( Topic -> Pauta, Vote -> Voto )

Para cadastrar uma pauta, deverá ser utilizado num `request POST` na url `... /api/v1/topic` a seguinte estrutura:

```shell
{
    "id":1,
    "description":"new topic",
    "timeLimit":30
}
```

Com id indicando o id da pauta, a descrição da pauta e o tempo limite de abertura da pauta em segundos ( caso não seja informado será aplicado 60 segundos )

Após uma pauta cadastrada, poderá ser votado nesta pauta com um `POST` na url `... /api/v1/vote` com a seguinte estrutura

```shell
{
    "id":1,
    "topicId":1,
    "associateId":62289608068,
    "value":0
}
```
O "id" representa o id do voto, o "topicId" representa o id da pauta, o "associateId" representa um CPF válido e o "value" representa o booleano do voto (SIM/NÃO) 

As informações sobre todos os votos e pautas do sistema estão disponíveis após cadastrados nas urls e são verificáveis com um `request GET`.

Também é possível deletar pautas utilizando um `request DELETE` especificando o id da pauta como na url: ... api/v1/topic/1

# Sobre a implementação

## Busca de CPFs válidos

Foi feito uma validação de CPFs utilizando-se da da url https://user-info.herokuapp.com/users/{cpf} com o cpf da url especificando um cpf válido, caso não seja permitido o voto do associado no sistema, o sistema lhe notificará

## Mensageria

Foi utilizado o RabbitMQ para o serviço de mensageria, implementei da seguinte forma, a cada voto cadastrado, haverá uma mensagem que informa os dados atualizados desta pauta, e além disso, assim que uma pauta é criada também é criado simultâneamente uma mensagem que só será entregue assim que o tempo de sessão desta pauta expirar, notificando-se o encerramento da pauta

Ps: Eu poderia ter colocado uma Thread separada que iria aguardar o fim da pauta e só depois enviaria a mensagem com os dados da pauta, porém eu não achei interessante criar uma Thread por pauta dado que o sistema poderia ter N pautas criadas...

## Performace

Foi utilizado "cacheamento" ou caching nesse sistema, em diferentes níveis de abstração, No 1º nível, é utilizado caching de forma que as requisições na minha camada de serviços irão fazer a busca no meu banco apenas a 1ª vez ( até que haja algum dado adicionado ou deletado ou alterado para evitar que os dados mantidos na cache estejam desatualizados, aí então haverá uma nova requisição ao banco )

No 2º nível de caching, as entidades específicas também serão mantidas em memória na cache para que caso sejam requisitadas mais de 1 vez, não há necessidade de busca no banco

Além disso também há uma adição de caching de queries que serve para que evite-se a criação de queries repetidas.

Os resultados de performace podem ser vistos nos testes pelas imagens ao se utilizar a ferramenta Jmeter.

### Rodando localmente com 1 milhão de usuários simultâneos fazendo 1 requisição no sistema ( 1 milhão de requisições )
Para um teste de resposta em 10ms - 13% das requisições passaram
![alt text](https://github.com/thePaulo/sic-votacao/blob/main/1kk.png?raw=true)

### Rodando localmente com 10 mil usuários simultâneos fazendo 1000 requisições no sistema ( 10 milhões de requisições )
Para um teste de resposta em 500ms - 66% das requições passaram
![alt text](https://github.com/thePaulo/sic-votacao/blob/main/10kk.png?raw=true)

### Rodando remotamente com 1000 usuários simultâneos fazendo 10 requisições no sistema ( 10 mil requisições )
Para um teste de resposta em 500ms - 60% das requisições passaram
![alt text](https://github.com/thePaulo/sic-votacao/blob/main/10k.png?raw=true)

## Versionamento da API

O versionamento poderia futuramente ser feito simplesmente pela alteração da URL do site modificando-se a sua versão `api/v??/...`

## Sobre os outros testes

Os testes unitários testam alguns dos casos em que o sistema deveria dar uma resposta negativa ao usuário.

Os testes de integração foram feitos em maior parte utilizando-se dos endpoints do sistema, portanto, após rodarem eles irão persistir no banco de dados suas informações e assim qualquer execução subsequente vai fazer com que estes testes falhem.


... Bem né, não sei mais o que podia ter feito nesse sistema, #paz...