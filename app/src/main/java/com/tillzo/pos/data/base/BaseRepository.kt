package com.tillzo.pos.data.base

/**
 * Marker interface for all Repository implementations.
 *
 * Architecture Law:
 * - ViewModels call UseCases, NOT Repositories.
 * - Repositories call DAOs (local) or DataSources (remote).
 * - No cross-module repository dependency allowed.
 */
interface BaseRepository
