FROM nextcloud

RUN openssl req -new -newkey rsa:4096 -days 365 -nodes -x509 \
    -subj "/C=DE/ST=NRW/L=Test/O=David-Development/CN=*" \
    -keyout /nextcloud.key  -out /nextcloud.cert

COPY ssl_config.append /etc/apache2/sites-available/nextcloud.conf

RUN a2ensite nextcloud.conf
RUN a2enmod rewrite
RUN a2enmod ssl
