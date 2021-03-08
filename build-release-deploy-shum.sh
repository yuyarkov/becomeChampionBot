mvn clean package
heroku container:push -a dev-telegram-champion-bot  worker
heroku container:release -a dev-telegram-champion-bot worker
