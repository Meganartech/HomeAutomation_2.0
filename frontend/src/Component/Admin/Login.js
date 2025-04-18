import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import axios from "axios";
import LoggedOutNavbar from "../LoggedOutNavbar";
import Swal from "sweetalert2";

export default function AdminLogin() {
    const [formData, setFormData] = useState({ username: "", password: "" });
    const navigate = useNavigate();

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            const response = await axios.post("http://localhost:8081/admin/login", formData,
                { headers: { "Content-Type": "application/json" }, }
            );
            if (response.data.token) {
                localStorage.setItem("token", response.data.token);
                localStorage.setItem("role", response.data.role);

                await Swal.fire({
                    icon: 'success',
                    title: 'Login Success',
                    text: 'You have been successfully logged in!',
                    showConfirmButton: false,
                    timer: 1500,
                    customClass: {
                        popup: 'my-swal-popup',
                        title: 'my-swal-title',
                        htmlContainer: 'my-swal-content',
                        confirmButton: 'my-swal-confirm-button',
                    }
                });

                console.log("Login success");
                navigate('/admin/profile');
                console.log("Login success");
            }
        } catch (err) {

            Swal.fire({
                icon: 'error',
                title: 'Login Failed',
                text: err.response?.data?.error || "Login failed",
                customClass: {
                    popup: 'my-swal-popup',
                    title: 'my-swal-title',
                    htmlContainer: 'my-swal-content',
                    confirmButton: 'my-swal-confirm-button',
                }
            });

            console.error("Login error:", err);
        }
    };

    return (
        <>
            {/* Navbar */}
            <LoggedOutNavbar />

            {/* Login form */}
            <div className="container mt-20">
                <div className="row justify-content-center">
                    <div className="col-xl-6 col-lg-8 col-md-10 col-sm-12">
                        <div className="card shadow border-0">
                            <div className="card-body">
                                <h4 className="card-title text-center text-123458">LOGIN</h4>
                                <form onSubmit={handleSubmit}>
                                    <div className="mb-3">
                                        <label className="form-label text-123458" htmlFor="username">Username</label>
                                        <input type="text" id="username" name="username" className="form-control" onChange={handleChange} required />
                                    </div>

                                    <div className="mb-3">
                                        <label className="form-label text-123458" htmlFor="password">Password</label>
                                        <input type="password" id="password" name="password" className="form-control" onChange={handleChange} required />
                                    </div>

                                    <div className="mb-3 form-check">
                                        <input type="checkbox" className="form-check-input" id="rememberMe" />
                                        <label className="form-check-labe text-123458" htmlFor="rememberMe">Remember me</label>
                                    </div>

                                    <div className="mb-3">
                                        <button type="submit" className="btn btn-123458 w-100">Log in</button>
                                    </div>
                                </form>

                                <div className="text-center">
                                    <small>
                                        <Link to="/forgot/password" className="text-123458">
                                            <u>Forgot Password?</u>
                                        </Link>
                                    </small>
                                    <br />
                                    <small>
                                        Don't have an account?{" "}
                                        <Link to="/user/register" className="text-123458">
                                            <u>Register here</u>
                                        </Link>
                                    </small>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </>
    );
}