# bot.sampo.Application
бот для записи на коллективные тренировки


**Как деплоить в хероку:**

Собрать jar.

`mvn clean package`


If you haven't already, log in to your Heroku account and follow the prompts to create a new SSH public key.

`heroku login`


Log in to Container Registry
You must have Docker set up locally to continue. You should see output when you run this command.

`docker ps`


Now you can sign into Container Registry.

`heroku container:login`


Push your Docker-based app
Build the Dockerfile in the current directory and push the Docker image.

`heroku container:push -a <app-name> <any-name-for-image>`


Deploy the changes
Release the newly pushed images to deploy your app.

`heroku container:release -a <app-name> <any-name-for-image>`


**Для локальной разрабоки** 

используйте профиль local-dev
Запустить локальную базу можно в докере командой:
docker run -p 5432:5432 --name champion-postgres -e POSTGRES_PASSWORD=mysecretpassword -d postgres