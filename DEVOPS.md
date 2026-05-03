# DevOps — CI/CD, Slack, Jenkins karşılaştırması, AWS

Bu doküman bitirme projesi **DevOps ve Deployment** maddeleri için özet ve kurulum notlarıdır.

---

## 1. GitHub Actions CI (`ci.yml`)

- **Tetikleyiciler:** `push` ve `pull_request` (`main` / `master`).
- **Adımlar:** JDK 21 (Temurin), Maven önbelleği, `./mvnw -B verify`.
- **Slack:** İşlem **her zaman** bittiğinde (`notify` job, `if: always()`), `verify` sonucu Slack’e gider — başarı veya başarısızlık.

### Slack Incoming Webhook (GitHub Secret)

1. Slack’te bir kanal için **Incoming Webhooks** (veya Slack API ile webhook URL’si) oluşturun ve URL’yi kopyalayın.
2. GitHub repo → **Settings → Secrets and variables → Actions → New repository secret**
   - Ad: `SLACK_WEBHOOK_URL`
   - Değer: webhook URL’si.
3. Secret yoksa workflow **sessizce** bildirimi atlar; CI yine çalışır.

---

## 2. Manuel deploy workflow (`deploy.yml`)

- **Actions → Deploy → Run workflow** ile elle çalıştırılır.
- Şu an AWS secret’ları yoksa job **başarıyla biter** ancak gerçek deploy yapmaz (`::notice` ile README/DEVOPS’a yönlendirir).
- AWS kimlik bilgileri eklendikten sonra dosya içindeki yorum satırlarındaki örnek `configure-aws-credentials` ve `eb deploy` adımları doldurulabilir.
- Deploy bitince **Slack** job’u (`notify-deploy`) aynı webhook ile özet gönderir (`SLACK_WEBHOOK_URL` tanımlıysa).

Önerilen ek Action secrets (üretim öncesi):

| Secret | Açıklama |
|--------|----------|
| `AWS_ACCESS_KEY_ID` | IAM kullanıcı erişim anahtarı |
| `AWS_SECRET_ACCESS_KEY` | IAM gizli anahtarı |
| `AWS_REGION` | Örn. `us-east-1` |
| `EB_ENVIRONMENT_NAME` veya uygulama adı | Elastic Beanstalk ortamı |

---

## 3. Jenkins vs GitHub Actions — kısa karşılaştırma

| Konu | Jenkins | GitHub Actions |
|------|---------|----------------|
| **Konum** | Genelde kendi sunucunuz / Kubernetes üzerinde uzun süre çalışan controller | GitHub’ın barındırdığı runner’lar veya self-hosted runner |
| **Pipeline tanımı** | Jenkinsfile (Declarative / Scripted Groovy), UI ile de job | Repo içinde YAML (`.github/workflows/*.yml`), versiyon kontrolü ile birlikte |
| **Tetikleme** | SCM webhook, zamanlanmış, manuel | `push`, `pull_request`, `workflow_dispatch`, `schedule`, vb. |
| **Gizli bilgi** | Credentials store, folder/job bazlı | Repository / environment **Secrets** |
| **Maliyet / işletme** | Sunucu bakımı, plugin güncellemeleri | Genel kullanımda GitHub kotası; self-hosted ile Jenkins’e yaklaşılabilir |
| **Bu projede** | Kurulu değil | `ci.yml` ile doğrulama + isteğe bağlı Slack; `deploy.yml` ile manuel deploy şablonu |

**Özet:** İkisi de “kaynak kod geldi → derle/test → (isteğe bağlı) deploy/bildirim” zincirini kurar. Jenkins daha fazla özelleştirme ve eski kurumsal entegrasyon için yaygın; GitHub Actions ise repo ile yakın entegrasyon ve düşük sürtünme sunar. Bu monorepo tesliminde pipeline mantığı **GitHub Actions** üzerinden gösterilmiştir.

---

## 4. AWS Elastic Beanstalk + RDS — kontrol listesi

**EB örnek zip’leri ve faz açıklamaları:** [elasticbeanstalk/README.md](elasticbeanstalk/README.md).

Özet:

1. **RDS PostgreSQL**; güvenlik grubunda **5432** için her EB ortamının **instance security group**’una izin.
2. **Veritabanları:** Auth / product / cart / order için ayrı veritabanı adları (init SQL ile oluşturulabilir).
3. **Ortam değişkenleri:** `APP_JWT_SECRET`, `COMMERCE_INTERNAL_SERVICE_TOKEN`, servisler arası URL’ler, İyzico callback’in kamuya açık adresi.
4. **ECR + Docker:** `linux/amd64` imaj push; `Dockerrun.aws.json` imaj URI’si ile uyumlu olmalı.
5. **HTTPS (isteğe bağlı):** API önüne ACM + ALB veya CloudFront.

---

## 5. Jib (Dockerfile olmadan imaj)

Çalışma zamanı modüllerinde **`jib-maven-plugin`** tanımlıdır. Üst `pom.xml` içinde `pluginManagement` ile sürüm ve varsayılan ayarlar merkezi tutulur.

| Özellik | Değer |
|--------|--------|
| Base image | `eclipse-temurin:21-jre-alpine` (`jib.from.image`) |
| Hedef image adı | `commerce/<artifactId>:<project.version>` |
| EXPOSE | Servise göre `8081` … `8085`; gateway `8080` |

**Yerel Docker’a yükleme:**

```bash
./mvnw -pl services/auth-service -am package jib:dockerBuild
```

**Tarball:**

```bash
./mvnw -pl services/auth-service -am package jib:buildTar
```

**Registry’ye göndermek:**

```bash
./mvnw -pl gateway/api-gateway -am package jib:build \
  -Djib.to.image=<ACCOUNT>.dkr.ecr.<REGION>.amazonaws.com/commerce/api-gateway:latest
```

Repo kökünde `./mvnw` dosyalarının bulunduğundan emin olun (CI için gerekli).

---

## 6. Loglama

Uygulama tarafında SLF4J ile standart loglar ve merkezi istisna işleyicileri kullanılır.
