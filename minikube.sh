#### kubectl
curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/arm64/kubectl"
sudo install -o root -g root -m 0755 kubectl -t /usr/local/bin/kubectl
kubectl version --client
rm kubectl
#### helm
sudo apt-get update
sudo apt-get -y install git
curl https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3 | bash
#### minikube
curl -LO https://storage.googleapis.com/minikube/releases/latest/minikube-linux-arm64
sudo install minikube-linux-arm64 /usr/local/bin/minikube
minikube version
rm minikube-linux-arm64
#### docker containerd
sudo install -m 0755 -d /etc/apt/keyrings
sudo curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
sudo chmod a+r /etc/apt/keyrings/docker.asc
echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu \
       $(. /etc/os-release && echo "${UBUNTU_CODENAME:-$VERSION_CODENAME}") stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
sudo apt-get update
sudo apt-get -y install docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
sudo usermod -aG docker $USER
#### start
minikube start --driver=docker --nodes=2
minikube addons enable dashboard
minikube addons enable metrics-server
#### grafana prometheus
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo update
helm upgrade --install prometheus prometheus-community/kube-prometheus-stack --create-namespace --namespace monitoring --set grafana.adminPassword=admin123
#### spring-boot-monitoring
git clone https://github.com/d-dmitriev/MephiAI.git
minikube image build -t mephi-ai:1.0 . --all
kubectl apply -f spring-boot-app.yaml
#### proxy
kubectl proxy --address="0.0.0.0" --disable-filter=true
kubectl port-forward svc/spring-boot-monitoring 8080:8080 --address 0.0.0.0
kubectl port-forward svc/prometheus-grafana 3000:80 -n monitoring --address 0.0.0.0
# Добавьте источник данных Prometheus (http://prometheus-kube-prometheus-prometheus:9090)
# Импортируйте дашборд 4701