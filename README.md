# JavaMoodleParser
[![Build Status](https://drone.darkhan.fun/api/badges/zhenya/JavaMoodleParser/status.svg)](https://drone.darkhan.fun/zhenya/JavaMoodleParser)

***

https://hub.docker.com/repository/docker/imjsman/moodle_parser_app

***

# Описание Проекта

Проект был создан ~~чтобы написать диплом~~ с целью помочь моим коллегам - студентам ТПУ.
(Пока альфа)
### Как использовать

После логина в приложении, программа собирает информацию с вашего аккаунта Moodle TPU (на это требуется немного времени). Структурирует, индексирует, для того чтобы потом вы могли легко найти все что вам нужно.
Забавный ~~фишка~~ баг - оказывается ваши работы с moodle доступны всем зарегистрировавшимся пользователям. Это позволяет вам легко обмениваться со всеми полезной информацией о том как решать задания.

### Интересные моменты при использовании

С помощью приложения вы можете вести поиск в реальном времени. Как в гугле - вводишь символ, показывается результат поиска.
Поиск ведется по всем текстам, а так же документам с текстовой информацией - doc, pdf и тд. (заложено в план разработки)

### Стек и принцип работы
Java, Spring, Postgres, Elasticsearch, Docker

С помощью Spring, парсим moodle, все данные отправляются в Postgres. Дополнительно из этих данных извлекается текст и отправляется в Elastic.
Да, данные дублируются. ORM для Elastic не подключен, и вообще используется elastic 7-ой версии, когда существует 8-я. Не было времени разбираться
Немного используются реактивные технологии. (в данном случае не повышают производительность, просто тестил)
Асинхронные запросы к moodle ограничены по количеству - есть защита на сайте.
Elasticsearch требует много оперативной памяти, т.к. я запускал на сервере с 2ГБ оперативки, пришлось увеличить swap до 1,5ГБ. Работает вроде.
Используется протокол Stomp для поиска в реальном времени.
Настроен CI/CD с помощью Gitea и Drone.


## Запуск с помощью Docker-compose

Это наиболее предпочтительный вариант для запуска
* Создайте директорию для проекта:

      mkdir moodleParser
* Перенесите docker-compose.yml в созданую директорию и перейдите в нее

      cp docker-compose.yml ./moodleParser/
      cd moodleParser
* Запустите Docker-compose:

      sudo docker-compose up -d

* Перейдите по ссылке для проверки работоспособности:

    [localhost:8080/status](localhost:8080/status)

## Сборка и запуск

Для сборки понадобится mvn
Для запуска java
* Сборка:

      mvn package -f pom.xml
* Запуск

      java - jar ./target/JavaMoodleParser.jar
* Или создать docker image. В корне проекта:

      sudo docker build --build-arg JAR_FILE=JavaMoodleParser.jar -t your_nickaname/moodle_parser_app:tagname .
* И запустить (см. подсказка ниже)

      sudo docker run  -d -p 8080:80 \
          -e DB_HOST=moodle_postgres \
          -e DB_NAME=moodleParser \
          -e DB_U_NAME=postgres \
          -e DB_U_PASS=2846 \
          -e EL_U_NAME=elastic \
          -e EL_U_PASS=asdfghjk \
          -e EL_HOSTNAME=moodle_elastic \
          your_nickaname/moodle_parser_app:tagname

## Описание переменных окружения

    |   ENV_VAR   | Описание                                   |
    |:-----------:|:-------------------------------------------| 
    |   DB_HOST   | Хостнэйм базы данных, например - localhost | 
    |   DB_NAME   | Название базы данных                       | 
    |  DB_U_NAME  | Имя пользователя базы данных               | 
    |  DB_U_PASS  | Пароль пользователя базы данных            | 
    |  EL_U_NAME  | Имя пользователя Elasticsearch             | 
    |  EL_U_PASS  | Пароль пользователя Elasticsearch          | 
    | EL_HOSTNAME | Хостнэйм Elasticsearch                     |



