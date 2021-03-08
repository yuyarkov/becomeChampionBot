mvn clean package
heroku container:push -a dev-become-champion-tg-bot  worker
heroku container:release -a dev-become-champion-tg-bot worker
