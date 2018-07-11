#FROM nextcloud:12-apache
FROM nextcloud:13.0.2-apache

RUN openssl req -new -newkey rsa:4096 -days 365 -nodes -x509 \
    -subj "/C=DE/ST=NRW/L=Test/O=David-Development/CN=*" \
    -keyout /nextcloud.key  -out /nextcloud.cert

COPY ssl_config.append /etc/apache2/sites-available/nextcloud.conf

RUN a2ensite nextcloud.conf
RUN a2enmod rewrite
RUN a2enmod ssl

#ENV NC_NEWS_VERSION=12.0.4
#RUN curl -L -o news.tar.gz https://github.com/nextcloud/news/releases/download/${NC_NEWS_VERSION}/news.tar.gz \
#    && tar -zxf news.tar.gz \
#    && rm news.tar.gz \
#    && mkdir -p /var/www/html/custom_apps/news \
#    && mv news /var/www/html/custom_apps \
#    && ls -la /var/www/html/custom_apps/news

