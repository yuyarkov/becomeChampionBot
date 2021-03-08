mvn clean package -Dmaven.test.skip=true
heroku container:push -a dev-telegram-champion-bot  worker
heroku container:release -a dev-telegram-champion-bot worker
