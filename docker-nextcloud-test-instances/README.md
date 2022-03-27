Start image with:

1. Start Container (`docker-compose up`)
2. Wait until setup is complete
3. Add IP as trusted domain (`nano nextcloud-data/config/config.php`)
  ```
  'trusted_domains' =>
    array (
      0 => 'localhost',
      1 => '192.168.1.100',
    ),
  ```
4. Open Nextcloud in Browser (e.g. http://192.168.1.100)
5. Setup News App
6. Add Nextcloud Account to your phone (in the nextcloud files app)
7. Use SSO in Nextcloud News App to access test instance


# Debug requests

curl -u admin http://localhost/index.php/apps/news/api/v1-2/items
curl -u admin http://localhost/index.php/apps/news/api/v1-2/items/updated?lastModified=1636295405&type=3&id=0 | jq '. | length'