package main

import (
	"bytes"
	"encoding/json"
	"fmt"
	"math/rand"
	"net/http"
	"sync"
	"sync/atomic"
	"time"
)

const (
	baseURL      = "http://localhost:8080"
	testDuration = 1 * time.Minute
	targetRPS    = 1000
)

type TransactionRequest struct {
	Valor    float64   `json:"valor"`
	DataHora time.Time `json:"dataHora"`
}

type Stats struct {
	TotalRequests   int64
	SuccessRequests int64
	FailedRequests  int64
	CreateRequests  int64
	DeleteRequests  int64
	StatRequests    int64
}

func main() {
	fmt.Println("🚀 Iniciando Load Test da API Itaú")
	fmt.Printf("📊 Target: %d req/s por %v\n", targetRPS, testDuration)
	fmt.Printf("🎯 Base URL: %s\n", baseURL)
	fmt.Println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

	if !checkAPIHealth() {
		fmt.Println("❌ API não está respondendo. Certifique-se que ela está rodando na porta 8080")
		return
	}

	stats := &Stats{}
	var wg sync.WaitGroup

	ticker := time.NewTicker(time.Second / time.Duration(targetRPS))
	defer ticker.Stop()

	done := make(chan bool)
	go func() {
		time.Sleep(testDuration)
		done <- true
	}()

	go showRealTimeStats(stats)

	fmt.Println("🔥 Iniciando bombardeio de requisições...")
	startTime := time.Now()

	for {
		select {
		case <-done:
			fmt.Println("\n⏰ Tempo esgotado! Finalizando teste...")
			goto finished
		case <-ticker.C:
			wg.Add(1)
			go func() {
				defer wg.Done()
				makeRandomRequest(stats)
			}()
		}
	}

finished:
	ticker.Stop()
	wg.Wait()

	duration := time.Since(startTime)
	printFinalStats(stats, duration)
}

func checkAPIHealth() bool {
	client := &http.Client{Timeout: 5 * time.Second}
	resp, err := client.Get(baseURL + "/estatistica")
	if err != nil {
		return false
	}
	defer resp.Body.Close()
	return resp.StatusCode == 200
}

func makeRandomRequest(stats *Stats) {
	atomic.AddInt64(&stats.TotalRequests, 1)

	randNum := rand.Intn(100)

	var success bool
	if randNum < 70 {
		success = createTransaction(stats)
		atomic.AddInt64(&stats.CreateRequests, 1)
	} else if randNum < 90 {
		success = getStatistics(stats)
		atomic.AddInt64(&stats.StatRequests, 1)
	} else {
		success = deleteTransactions(stats)
		atomic.AddInt64(&stats.DeleteRequests, 1)
	}

	if success {
		atomic.AddInt64(&stats.SuccessRequests, 1)
	} else {
		atomic.AddInt64(&stats.FailedRequests, 1)
	}
}

func createTransaction(stats *Stats) bool {

	transaction := TransactionRequest{
		Valor:    rand.Float64() * 1000,
		DataHora: time.Now().Add(-time.Duration(rand.Intn(60)) * time.Second),
	}

	jsonData, err := json.Marshal(transaction)
	if err != nil {
		return false
	}

	client := &http.Client{Timeout: 10 * time.Second}
	resp, err := client.Post(baseURL+"/transacao", "application/json", bytes.NewBuffer(jsonData))
	if err != nil {
		return false
	}
	defer resp.Body.Close()

	return resp.StatusCode == 201
}

func getStatistics(stats *Stats) bool {
	client := &http.Client{Timeout: 10 * time.Second}
	resp, err := client.Get(baseURL + "/estatistica")
	if err != nil {
		return false
	}
	defer resp.Body.Close()

	return resp.StatusCode == 200
}

func deleteTransactions(stats *Stats) bool {
	client := &http.Client{Timeout: 10 * time.Second}
	req, err := http.NewRequest("DELETE", baseURL+"/transacao", nil)
	if err != nil {
		return false
	}

	resp, err := client.Do(req)
	if err != nil {
		return false
	}
	defer resp.Body.Close()

	return resp.StatusCode == 204
}

func showRealTimeStats(stats *Stats) {
	ticker := time.NewTicker(5 * time.Second)
	defer ticker.Stop()

	for range ticker.C {
		total := atomic.LoadInt64(&stats.TotalRequests)
		success := atomic.LoadInt64(&stats.SuccessRequests)
		failed := atomic.LoadInt64(&stats.FailedRequests)
		creates := atomic.LoadInt64(&stats.CreateRequests)
		deletes := atomic.LoadInt64(&stats.DeleteRequests)
		statReqs := atomic.LoadInt64(&stats.StatRequests)

		if total > 0 {
			successRate := float64(success) / float64(total) * 100
			fmt.Printf("📈 Total: %d | ✅ Success: %d (%.1f%%) | ❌ Failed: %d | POST: %d | GET: %d | DELETE: %d\n",
				total, success, successRate, failed, creates, statReqs, deletes)
		}
	}
}

func printFinalStats(stats *Stats, duration time.Duration) {
	total := atomic.LoadInt64(&stats.TotalRequests)
	success := atomic.LoadInt64(&stats.SuccessRequests)
	failed := atomic.LoadInt64(&stats.FailedRequests)
	creates := atomic.LoadInt64(&stats.CreateRequests)
	deletes := atomic.LoadInt64(&stats.DeleteRequests)
	statReqs := atomic.LoadInt64(&stats.StatRequests)

	fmt.Println("\n" + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
	fmt.Println("🏁 RESULTADOS FINAIS DO LOAD TEST")
	fmt.Println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
	fmt.Printf("⏱️  Duração: %v\n", duration.Round(time.Second))
	fmt.Printf("📊 Total de Requisições: %d\n", total)
	fmt.Printf("✅ Requisições Bem-sucedidas: %d\n", success)
	fmt.Printf("❌ Requisições Falharam: %d\n", failed)

	if total > 0 {
		successRate := float64(success) / float64(total) * 100
		avgRPS := float64(total) / duration.Seconds()
		fmt.Printf("📈 Taxa de Sucesso: %.2f%%\n", successRate)
		fmt.Printf("🚀 RPS Médio: %.2f req/s\n", avgRPS)
	}

	fmt.Println("\n📊 DISTRIBUIÇÃO POR ENDPOINT:")
	fmt.Printf("  POST /transacao: %d requisições\n", creates)
	fmt.Printf("  GET /estatistica: %d requisições\n", statReqs)
	fmt.Printf("  DELETE /transacao: %d requisições\n", deletes)
	fmt.Println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
}
