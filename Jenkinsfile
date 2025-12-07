pipeline {
    agent any

    environment {
        TENANT_ID = "${TENANT_ID}"
        TENANT_DOMAIN = "${TENANT_DOMAIN}"
        COMPOSE_DIR = "/opt/kompu/tenants"
        NGINX_CONF_DIR = "/opt/kompu/nginx/conf.d"
        BASE_IMAGE = "kompu-tenant-web:latest"
        NGINX_CONTAINER = "kompu-nginx"
    }

    stages {

        stage('Validate Inputs') {
            steps {
                script {
                    if (!TENANT_ID || !TENANT_DOMAIN) {
                        error("TENANT_ID and TENANT_DOMAIN must be provided.")
                    }
                    echo "Provisioning tenant ${TENANT_ID} on ${TENANT_DOMAIN}"
                }
            }
        }

        stage('Generate Tenant Compose') {
            steps {
                sh """
                mkdir -p ${COMPOSE_DIR}/${TENANT_ID}

                cat <<EOF > ${COMPOSE_DIR}/${TENANT_ID}/docker-compose.yml
                services:
                  tenant-web-${TENANT_ID}:
                    image: ${BASE_IMAGE}
                    container_name: tenant-web-${TENANT_ID}
                    restart: always
                    environment:
                      - TENANT_ID=${TENANT_ID}
                      - TENANT_DOMAIN=${TENANT_DOMAIN}
                    expose:
                      - "3000"
                    networks:
                      - kompu-net
                networks:
                  kompu-net:
                    external: true
                EOF
                """
            }
        }

        stage('Start Web Container') {
            steps {
                sh """
                docker compose -f ${COMPOSE_DIR}/${TENANT_ID}/docker-compose.yml up -d
                """
            }
        }

        stage('Register Nginx Routing') {
            steps {
                sh """
                cat <<EOF > ${NGINX_CONF_DIR}/tenant-${TENANT_ID}.conf
                server {
                    listen 80;
                    server_name ${TENANT_DOMAIN};

                    location / {
                        proxy_pass http://tenant-web-${TENANT_ID}:3000;
                        proxy_set_header Host \$host;
                        proxy_set_header X-Real-IP \$remote_addr;
                        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
                        proxy_set_header X-Forwarded-Proto \$scheme;
                    }
                }
                EOF
                """
            }
        }

        stage('Reload Nginx') {
            steps {
                sh """
                docker exec ${NGINX_CONTAINER} nginx -t
                docker exec ${NGINX_CONTAINER} nginx -s reload
                """
            }
        }

        stage('Smoke Test') {
            steps {
                sh "curl -I http://${TENANT_DOMAIN} || true"
            }
        }
    }

    post {
        success {
            echo "Tenant ${TENANT_ID} successfully deployed at https://${TENANT_DOMAIN}"
        }
        failure {
            echo "Rollback starting..."

            sh """
            docker compose -f ${COMPOSE_DIR}/${TENANT_ID}/docker-compose.yml down || true
            rm -rf ${COMPOSE_DIR}/${TENANT_ID} || true
            rm -f ${NGINX_CONF_DIR}/tenant-${TENANT_ID}.conf || true
            docker exec ${NGINX_CONTAINER} nginx -s reload || true
            """
        }
    }
}
