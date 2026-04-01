FROM eclipse-temurin:17-jdk-jammy

RUN apt-get update && apt-get install -y wget unzip && rm -rf /var/lib/apt/lists/*

ENV ANDROID_HOME=/opt/android-sdk
ENV PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools

RUN mkdir -p $ANDROID_HOME/cmdline-tools && \
    wget -q https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip \
         -O /tmp/cmdtools.zip && \
    unzip -q /tmp/cmdtools.zip -d /tmp && \
    mv /tmp/cmdline-tools $ANDROID_HOME/cmdline-tools/latest && \
    rm /tmp/cmdtools.zip

RUN yes | sdkmanager --licenses > /dev/null && \
    sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0"

WORKDIR /app
COPY . .

RUN chmod +x gradlew && ./gradlew assembleDebug --no-daemon
