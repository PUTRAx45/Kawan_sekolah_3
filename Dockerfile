FROM node:20-slim

RUN apt-get update && apt-get install -y default-jre-headless && rm -rf /var/lib/apt/lists/*
RUN npm install -g firebase-tools

WORKDIR /app
COPY firebase.json firestore.rules ./

EXPOSE 4000 8080 9099

CMD ["firebase", "emulators:start", "--project", "demo-kawansekolah"]
