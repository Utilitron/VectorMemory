# VectorMemory 🧠
A resource-aware Java library for LLM agent memory. It implements a cognitive-inspired dual-tier system: a fast, HNSW-backed Short-Term Memory (STM) and a persistent, distilled Long-Term Memory (LTM).

Unlike standard vector databases, VectorMemory is built to be resource-aware, automatically triggering memory consolidation and distillation when system pressure (RAM/VRAM) is detected.

This project is currently in active development and is not yet available on Maven Central. Follow the instructions below to build it from source.

🚀 Key Features
Dual-Tier Architecture: Manage transient "working" context (STM) and permanent "learned" knowledge (LTM).

HNSW Vector Indexing: Powered by JVector for lightning-fast concurrent similarity searches.

Saliency-Based Retention: Memories are scored based on a weighted formula of Recency, Importance, and Frequency.

Provider-Agnostic LLM Integration: Easily plug in LlamaFFM, Ollama, or cloud APIs for embedding and distillation.

Zero-Dependency Core: The core logic remains decoupled from specific storage backends or LLM implementations.
